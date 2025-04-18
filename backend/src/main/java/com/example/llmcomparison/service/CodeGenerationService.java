package com.example.llmcomparison.service;

import com.example.llmcomparison.model.GenerationRequest;
import com.example.llmcomparison.model.GenerationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CodeGenerationService {

    @Value("${mistral.api.key}")
    private String mistralApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String MISTRAL_API_URL = "https://api.mistral.ai/v1/chat/completions";

    public GenerationResponse generate(GenerationRequest request) {
        GenerationResponse response = new GenerationResponse();
        List<GenerationResponse.ModelResult> results = new ArrayList<>();

        // Log the generation request
        System.out.println("Code Generation Request:");
        System.out.println("Prompt: " + request.getPrompt());
        System.out.println("Models: " + request.getModels());

        for (String model : request.getModels()) {
            String output = generateCodeForModel(model, request.getPrompt());
            GenerationResponse.ModelResult result = new GenerationResponse.ModelResult();
            result.setModel(model);
            result.setOutput(output);
            results.add(result);
        }

        response.setResults(results);
        return response;
    }

    private String generateCodeForModel(String modelId, String prompt) {
        // Add Mistral model to the switch case
        switch (modelId) {
            case "starcoder":
                return generateStarCoderResponse(prompt);
            case "gpt35":
                return generateGPT35Response(prompt);
            case "codellama":
                return generateCodeLlamaResponse(prompt);
            case "mistral":
                return generateMistralResponse(prompt);
            default:
                return "// Unknown model\nfunction notImplemented() {\n  console.log('Model not implemented');\n}";
        }
    }

    private String generateMistralResponse(String prompt) {
        try {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + mistralApiKey);

            // Prepare request body based on the Mistral API documentation
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "mistral-small-latest");
            requestBody.put("temperature", 0.2); // Lower temperature for more deterministic code generation
            requestBody.put("max_tokens", 1024); // Set a reasonable limit for code generation
            
            // Create messages array with user prompt
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", "Write code to solve the following task. Provide only the code without explanations: " + prompt);
            messages.add(message);
            
            requestBody.put("messages", messages);
            
            // Add response format for text
            Map<String, Object> responseFormat = new HashMap<>();
            responseFormat.put("type", "text");
            requestBody.put("response_format", responseFormat);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make API call
            Map<String, Object> response = restTemplate.postForObject(MISTRAL_API_URL, entity, Map.class);

            // Extract response based on the Mistral API response format
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> messageResponse = (Map<String, Object>) choice.get("message");
                    if (messageResponse != null && messageResponse.containsKey("content")) {
                        return (String) messageResponse.get("content");
                    }
                }
            }
            
            return "// Error: Failed to parse Mistral API response";
        } catch (Exception e) {
            System.err.println("Error calling Mistral API: " + e.getMessage());
            e.printStackTrace();
            return "// Error: " + e.getMessage();
        }
    }

    private String generateStarCoderResponse(String prompt) {
        if (prompt.contains("palindrome")) {
            return "function longestPalindrome(s) {\n" +
                   "  // Edge cases\n" +
                   "  if (!s || s.length <= 1) return s;\n" +
                   "  \n" +
                   "  let start = 0, maxLength = 1;\n" +
                   "  \n" +
                   "  function expandAroundCenter(left, right) {\n" +
                   "    while (left >= 0 && right < s.length && s[left] === s[right]) {\n" +
                   "      const currLength = right - left + 1;\n" +
                   "      if (currLength > maxLength) {\n" +
                   "        maxLength = currLength;\n" +
                   "        start = left;\n" +
                   "      }\n" +
                   "      left--;\n" +
                   "      right++;\n" +
                   "    }\n" +
                   "  }\n" +
                   "  \n" +
                   "  for (let i = 0; i < s.length; i++) {\n" +
                   "    // Handle odd length palindromes\n" +
                   "    expandAroundCenter(i, i);\n" +
                   "    // Handle even length palindromes\n" +
                   "    expandAroundCenter(i, i + 1);\n" +
                   "  }\n" +
                   "  \n" +
                   "  return s.substring(start, start + maxLength);\n" +
                   "}";
        } else if (prompt.contains("sort")) {
            return "// StarCoder implementation\n" +
                   "function quickSort(arr) {\n" +
                   "  if (arr.length <= 1) return arr;\n" +
                   "  \n" +
                   "  const pivot = arr[Math.floor(arr.length / 2)];\n" +
                   "  const left = arr.filter(x => x < pivot);\n" +
                   "  const middle = arr.filter(x => x === pivot);\n" +
                   "  const right = arr.filter(x => x > pivot);\n" +
                   "  \n" +
                   "  return [...quickSort(left), ...middle, ...quickSort(right)];\n" +
                   "}";
        } else {
            return "// StarCoder implementation\n" +
                   "function solve(input) {\n" +
                   "  // Parse the input\n" +
                   "  const parsed = input.trim().split('\\n');\n" +
                   "  \n" +
                   "  // Process the data\n" +
                   "  const result = parsed.map(line => {\n" +
                   "    return line.toUpperCase();\n" +
                   "  });\n" +
                   "  \n" +
                   "  return result.join('\\n');\n" +
                   "}";
        }
    }

    private String generateGPT35Response(String prompt) {
        if (prompt.contains("palindrome")) {
            return "/**\n" +
                   " * Finds the longest palindrome substring in a string\n" +
                   " * @param {string} s - The input string\n" +
                   " * @return {string} - The longest palindrome substring\n" +
                   " */\n" +
                   "function findLongestPalindrome(s) {\n" +
                   "  if (!s || s.length < 1) return '';\n" +
                   "  \n" +
                   "  let start = 0;\n" +
                   "  let maxLength = 1;\n" +
                   "  \n" +
                   "  // Helper function to expand around center\n" +
                   "  function expandFromCenter(left, right) {\n" +
                   "    while (left >= 0 && right < s.length && s[left] === s[right]) {\n" +
                   "      const currentLength = right - left + 1;\n" +
                   "      if (currentLength > maxLength) {\n" +
                   "        maxLength = currentLength;\n" +
                   "        start = left;\n" +
                   "      }\n" +
                   "      left--;\n" +
                   "      right++;\n" +
                   "    }\n" +
                   "  }\n" +
                   "  \n" +
                   "  for (let i = 0; i < s.length; i++) {\n" +
                   "    // Check for odd length palindromes (e.g., 'racecar')\n" +
                   "    expandFromCenter(i, i);\n" +
                   "    \n" +
                   "    // Check for even length palindromes (e.g., 'abba')\n" +
                   "    expandFromCenter(i, i + 1);\n" +
                   "  }\n" +
                   "  \n" +
                   "  return s.substring(start, start + maxLength);\n" +
                   "}\n" +
                   "\n" +
                   "// Example usage:\n" +
                   "// console.log(findLongestPalindrome('babad')); // 'bab' or 'aba'\n" +
                   "// console.log(findLongestPalindrome('cbbd')); // 'bb'";
        } else if (prompt.contains("sort")) {
            return "/**\n" +
                   " * Implementation of quick sort algorithm\n" +
                   " * @param {number[]} arr - Array to be sorted\n" +
                   " * @return {number[]} - Sorted array\n" +
                   " */\n" +
                   "function quickSort(arr) {\n" +
                   "  // Base case: arrays with 0 or 1 element are already sorted\n" +
                   "  if (arr.length <= 1) {\n" +
                   "    return arr;\n" +
                   "  }\n" +
                   "  \n" +
                   "  // Choose pivot (here we use the middle element)\n" +
                   "  const pivotIndex = Math.floor(arr.length / 2);\n" +
                   "  const pivot = arr[pivotIndex];\n" +
                   "  \n" +
                   "  // Partition the array\n" +
                   "  const less = [];\n" +
                   "  const equal = [];\n" +
                   "  const greater = [];\n" +
                   "  \n" +
                   "  for (let element of arr) {\n" +
                   "    if (element < pivot) {\n" +
                   "      less.push(element);\n" +
                   "    } else if (element === pivot) {\n" +
                   "      equal.push(element);\n" +
                   "    } else {\n" +
                   "      greater.push(element);\n" +
                   "    }\n" +
                   "  }\n" +
                   "  \n" +
                   "  // Recursively sort the sub-arrays and combine the result\n" +
                   "  return [...quickSort(less), ...equal, ...quickSort(greater)];\n" +
                   "}\n" +
                   "\n" +
                   "// Example usage:\n" +
                   "// const unsortedArray = [3, 6, 8, 10, 1, 2, 1];\n" +
                   "// console.log(quickSort(unsortedArray)); // [1, 1, 2, 3, 6, 8, 10]";
        } else {
            return "/**\n" +
                   " * Solution for the given problem\n" +
                   " * @param {string} input - The input data\n" +
                   " * @return {string} - The processed output\n" +
                   " */\n" +
                   "function processData(input) {\n" +
                   "  // Parse input\n" +
                   "  const lines = input.trim().split('\\n');\n" +
                   "  \n" +
                   "  // Process each line\n" +
                   "  const results = lines.map(line => {\n" +
                   "    // Apply transformation\n" +
                   "    return line.toUpperCase();\n" +
                   "  });\n" +
                   "  \n" +
                   "  // Return formatted output\n" +
                   "  return results.join('\\n');\n" +
                   "}\n" +
                   "\n" +
                   "// Example usage:\n" +
                   "// const input = 'hello\\nworld';\n" +
                   "// console.log(processData(input)); // 'HELLO\\nWORLD'";
        }
    }

    private String generateCodeLlamaResponse(String prompt) {
        if (prompt.contains("palindrome")) {
            return "def longest_palindrome(s: str) -> str:\n" +
                   "    \"\"\"Find the longest palindrome substring in a string.\n" +
                   "    \n" +
                   "    Args:\n" +
                   "        s: Input string\n" +
                   "        \n" +
                   "    Returns:\n" +
                   "        Longest palindrome substring\n" +
                   "    \"\"\"\n" +
                   "    if not s:\n" +
                   "        return \"\"\n" +
                   "        \n" +
                   "    # Initialize variables to track the longest palindrome\n" +
                   "    start = 0\n" +
                   "    max_len = 1\n" +
                   "    \n" +
                   "    # Helper function to expand around center\n" +
                   "    def expand_from_center(left: int, right: int) -> None:\n" +
                   "        nonlocal start, max_len\n" +
                   "        \n" +
                   "        while left >= 0 and right < len(s) and s[left] == s[right]:\n" +
                   "            curr_len = right - left + 1\n" +
                   "            if curr_len > max_len:\n" +
                   "                max_len = curr_len\n" +
                   "                start = left\n" +
                   "            left -= 1\n" +
                   "            right += 1\n" +
                   "    \n" +
                   "    # Check each possible center position\n" +
                   "    for i in range(len(s)):\n" +
                   "        # Odd length palindromes (center is a single character)\n" +
                   "        expand_from_center(i, i)\n" +
                   "        \n" +
                   "        # Even length palindromes (center is between two characters)\n" +
                   "        expand_from_center(i, i + 1)\n" +
                   "    \n" +
                   "    return s[start:start + max_len]\n" +
                   "\n" +
                   "# Test cases\n" +
                   "assert longest_palindrome(\"babad\") in [\"bab\", \"aba\"]\n" +
                   "assert longest_palindrome(\"cbbd\") == \"bb\"\n" +
                   "assert longest_palindrome(\"a\") == \"a\"\n" +
                   "assert longest_palindrome(\"\") == \"\"";
        } else if (prompt.contains("sort")) {
            return "def quick_sort(arr):\n" +
                   "    \"\"\"Implementation of quick sort algorithm.\n" +
                   "    \n" +
                   "    Args:\n" +
                   "        arr: List to be sorted\n" +
                   "        \n" +
                   "    Returns:\n" +
                   "        Sorted list\n" +
                   "    \"\"\"\n" +
                   "    # Base case: empty lists or lists with one element are already sorted\n" +
                   "    if len(arr) <= 1:\n" +
                   "        return arr\n" +
                   "    \n" +
                   "    # Select pivot (using middle element)\n" +
                   "    pivot_idx = len(arr) // 2\n" +
                   "    pivot = arr[pivot_idx]\n" +
                   "    \n" +
                   "    # Partition the array\n" +
                   "    left = [x for x in arr if x < pivot]\n" +
                   "    middle = [x for x in arr if x == pivot]\n" +
                   "    right = [x for x in arr if x > pivot]\n" +
                   "    \n" +
                   "    # Recursively sort the sub-lists and combine them\n" +
                   "    return quick_sort(left) + middle + quick_sort(right)\n" +
                   "\n" +
                   "# Example usage\n" +
                   "if __name__ == \"__main__\":\n" +
                   "    test_list = [3, 6, 8, 10, 1, 2, 1]\n" +
                   "    sorted_list = quick_sort(test_list)\n" +
                   "    print(sorted_list)  # Output: [1, 1, 2, 3, 6, 8, 10]";
        } else {
            return "def process_data(input_text):\n" +
                   "    \"\"\"Process the input data according to requirements.\n" +
                   "    \n" +
                   "    Args:\n" +
                   "        input_text: String containing input data\n" +
                   "        \n" +
                   "    Returns:\n" +
                   "        Processed output string\n" +
                   "    \"\"\"\n" +
                   "    # Split the input into lines\n" +
                   "    lines = input_text.strip().split('\\n')\n" +
                   "    \n" +
                   "    # Process each line\n" +
                   "    processed_lines = []\n" +
                   "    for line in lines:\n" +
                   "        # Convert to uppercase as an example transformation\n" +
                   "        processed_line = line.upper()\n" +
                   "        processed_lines.append(processed_line)\n" +
                   "    \n" +
                   "    # Join the processed lines back together\n" +
                   "    return '\\n'.join(processed_lines)\n" +
                   "\n" +
                   "# Example usage\n" +
                   "if __name__ == \"__main__\":\n" +
                   "    sample_input = \"hello\\nworld\"\n" +
                   "    result = process_data(sample_input)\n" +
                   "    print(result)  # Output: \"HELLO\\nWORLD\"";
        }
    }
}