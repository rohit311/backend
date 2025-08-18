Spring AI + MCP Server using Ollama

- Ollama model : `phi3:mini`


Steps to start Ollama & Spring appplication :
1. List all the avaialble models using - `ollama list`
2. If `phi3:mini` is not listed, install using - `ollama pull phi3:mini`
3. Remove any unused model using - `ollama rm <model-name>`
4. Strat spring boot app using - `mvn:spring-bot:run`

Below models do not work with Spring AI tooling:
- qwen2.5:1.5b
- qwen2.5:3b
