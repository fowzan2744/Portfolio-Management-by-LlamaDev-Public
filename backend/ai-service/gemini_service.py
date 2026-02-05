"""
Gemini AI Microservice for Portfolio Analysis
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
from google import genai
from dotenv import load_dotenv
import os
import json
import re
import logging

# Load environment variables from .env file
load_dotenv()

app = Flask(__name__)
CORS(app)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

GEMINI_API_KEY = os.getenv('GEMINI_API_KEY', '')
GEMINI_MODEL = os.getenv('GEMINI_MODEL', 'gemini-2.0-flash-exp')

def get_gemini_client():
    if not GEMINI_API_KEY:
        raise ValueError("GEMINI_API_KEY not set")
    return genai.Client(api_key=GEMINI_API_KEY)

@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({
        'status': 'healthy',
        'service': 'gemini-ai-service',
        'api_key_configured': bool(GEMINI_API_KEY)
    })

def fix_malformed_json(text):
    """Attempt to fix common JSON formatting issues"""
    # Remove any BOM or invisible characters
    text = text.strip().lstrip('\ufeff')
    
    # Fix common issues with escaped characters
    # Replace smart quotes with regular quotes
    text = text.replace('"', '"').replace('"', '"')
    text = text.replace(''', "'").replace(''', "'")
    
    # Try to fix truncated JSON by closing open structures
    open_braces = text.count('{') - text.count('}')
    open_brackets = text.count('[') - text.count(']')
    open_quotes = text.count('"') % 2
    
    if open_quotes == 1:
        # Close unterminated string
        text += '"'
    
    if open_brackets > 0:
        text += ']' * open_brackets
    
    if open_braces > 0:
        text += '}' * open_braces
    
    return text

@app.route('/api/ai/analyze', methods=['POST'])
def analyze_portfolio():
    try:
        data = request.get_json()
        prompt = data.get('prompt', '')
        
        if not prompt:
            return jsonify({'error': 'No prompt provided'}), 400
        
        logger.info("Generating AI insights...")
        client = get_gemini_client()
        
        # Enhanced prompt with strict JSON requirements
        enhanced_prompt = prompt + """

CRITICAL INSTRUCTIONS:
1. Return ONLY valid, complete JSON
2. Do NOT truncate strings - complete all text
3. Properly escape quotes and special characters
4. Ensure all strings are properly terminated
5. Do NOT include any text outside the JSON object
6. Keep responses concise to avoid truncation"""
        
        response = client.models.generate_content(
            model=GEMINI_MODEL,
            contents=enhanced_prompt,
            config={
                'temperature': 0.3,
                'top_p': 0.85,
                'top_k': 20,
                'max_output_tokens': 4096,
                'response_mime_type': 'application/json'
            }
        )
        
        ai_response = response.text.strip()
        logger.info(f"AI insights generated successfully (length: {len(ai_response)} chars)")
        
        # Clean the response - remove markdown code blocks if present
        if ai_response.startswith('```'):
            lines = ai_response.split('\n')
            if lines[0].startswith('```'):
                lines = lines[1:]
            if lines and lines[-1].strip() == '```':
                lines = lines[:-1]
            ai_response = '\n'.join(lines).strip()
        
        # Try to parse JSON with multiple strategies
        insights = None
        parse_errors = []
        
        # Strategy 1: Direct parse
        try:
            insights = json.loads(ai_response)
            logger.info("JSON parsed successfully (direct)")
        except json.JSONDecodeError as je:
            parse_errors.append(f"Direct parse: {str(je)}")
            logger.warning(f"Direct JSON parse failed: {je}")
            
            # Strategy 2: Try to fix malformed JSON
            try:
                fixed_response = fix_malformed_json(ai_response)
                insights = json.loads(fixed_response)
                logger.info("JSON parsed successfully (after fixing)")
            except json.JSONDecodeError as je2:
                parse_errors.append(f"After fixing: {str(je2)}")
                logger.warning(f"Fixed JSON parse failed: {je2}")
                
                # Strategy 3: Extract JSON object with regex
                try:
                    json_match = re.search(r'\{[^{}]*(?:\{[^{}]*\}[^{}]*)*\}', ai_response, re.DOTALL)
                    if json_match:
                        extracted = json_match.group(0)
                        insights = json.loads(extracted)
                        logger.info("JSON parsed successfully (regex extraction)")
                    else:
                        raise ValueError("No JSON object found in response")
                except Exception as e3:
                    parse_errors.append(f"Regex extraction: {str(e3)}")
                    logger.error(f"All JSON parsing strategies failed")
                    logger.error(f"Parse errors: {parse_errors}")
                    logger.error(f"Full response (first 1000 chars): {ai_response[:1000]}")
                    raise ValueError(f"Failed to parse JSON response after all strategies. Errors: {'; '.join(parse_errors)}")
        
        if insights is None:
            raise ValueError("Could not parse AI response as JSON")
        
        return jsonify(insights), 200
            
    except Exception as e:
        logger.error(f"Error: {str(e)}", exc_info=True)
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    if GEMINI_API_KEY:
        logger.info(f" GEMINI_API_KEY configured ({len(GEMINI_API_KEY)} chars)")
    else:
        logger.warning(" GEMINI_API_KEY not set!")
    
    port = int(os.getenv('PORT', 5000))
    app.run(host='0.0.0.0', port=port, debug=False)
