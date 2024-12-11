# Sanford

## Considerations

* Machine Learning and Artificial Intelligence applications have unique computing demands.
* Start with [foundation models](https://cset.georgetown.edu/article/what-are-generative-ai-large-language-models-and-foundation-models/).
* Use cases will drive model selection.  Not all models are created equally.  Experimentation is strongly encouraged.  
* Does your organization have the "will and skill" to procure, operate, scale, and maintain compute or will you rely on commercially hosted options? 
* Enterprises have sunk cost in heritage compute infrastructure (e.g., [pre-Skylake](https://en.wikipedia.org/wiki/List_of_Intel_Xeon_processors) Xeon release).  While it's possible to run GenAI workloads on this compute it's not recommended for production (innovation will be hampered based on slow response times).
  * No "one size fits all" approach to specification and investment exists.

## Compute

Rules of thumb (*Q4 2024):

* [ARM](https://www.arm.com/) processors are emerging as standard for decentralized and on-device Gen AI applications

* High memory bandwidth is important, if not ARM, choose a modern processor like Intel's 4th Gen Sapphire Rapids Architecture

    > 4th Generation Intel® Xeon® Scalable Processors, known as Sapphire Rapids processors, are the designated CPU for new Dell PowerEdge servers.  Compared to prior-gen 3rd Generation Intel® Xeon® Scalable Processors, Sapphire Rapids Architecture supports up to 50% higher memory bandwidth (4800MTS (1DPC)/4400MTS(2DPC) on 4th Gen Intel® Xeon® Scalable Processors vs 3200MT/s on Ice Lake Processors).

* At least 4 cores for each GPU accelerator

    > If your workload has a significant CPU compute component then 32 or even 64 cores could be ideal. In any case, a 16-core processor would generally be considered minimal.

* PCIe lanes for up to four GPUs

    > Both Intel Xeon W-3500 and AMD Threadripper PRO 7000 Series support enough PCIe lanes for three or four GPUs (depending on motherboard layout, chassis space, and power draw). This class of processors also supports 8 memory channels, which can have a significant impact on performance for CPU-bound workloads.

## LLMs and Vector Databases

* [Models leaderboard](https://artificialanalysis.ai/leaderboards/models)
* [MTEB leaderboard](https://huggingface.co/spaces/mteb/leaderboard)
* Chat model comparison - [Spring AI](https://docs.spring.io/spring-ai/reference/api/chat/comparison.html), [Artificial Analysis AI Insights](https://artificialanalysis.ai/insights/chatbots-comparison)
* [Vector database comparison](https://superlinked.com/vector-db-comparison)

## Suggested Reading

This curated collection of articles stands to influence your specification and investment decision criteria.

* [What's Driving the Hype Cycle for Generative AI, 2024](https://www.gartner.com/en/articles/hype-cycle-for-genai)
* [AI vs. Generative AI: The Differences Explained](https://www.coursera.org/articles/ai-vs-generative-ai)
* [Choosing Your Path in Generative AI: Open-Source or Proprietary?](https://attri.ai/blog/choosing-your-path-in-generative-ai-open-source-or-proprietary)
* [Facing GPU Shortages and Rising Cloud Costs in the Era of GenAI](https://generativeai.pub/facing-gpu-shortages-and-rising-cloud-costs-in-the-era-of-genai-7908420a8d79)
* [Hardware Recommendations for Machine Learning and Artificial Intelligence](https://www.pugetsystems.com/solutions/ai-and-hpc-workstations/machine-learning-ai/hardware-recommendations)
* [CPU vs GPU for Machine Learning](https://blog.purestorage.com/purely-educational/cpu-vs-gpu-for-machine-learning)
* [Why do I need a GPU for ML/AI](https://www.reddit.com/r/learnmachinelearning/comments/184so8i/why_do_i_need_a_gpu_for_mlai)
* [Do we need GPUs/NPUs for local AI?](https://medium.com/@andreask_75652/do-we-need-gpus-npus-for-local-ai-b6cd9b60f00c)
* [Buying a PC for Local AI? These are the specs taht actually matter](https://www.theregister.com/2024/08/25/ai_pc_buying_guide/)
* [How to run LLMs with less GPU and CPU memory](https://medium.com/data-science-in-your-pocket/how-to-run-llms-in-less-gpu-and-cpu-memory-6989e6ec5621)
* [LLM Inference – Consumer GPU performance](https://www.pugetsystems.com/labs/articles/llm-inference-consumer-gpu-performance/)
* [The Hidden Bottleneck: How GPU Memory Hierarchy Affects Your Computing Experience](https://www.digitalocean.com/community/tutorials/the-hidden-bottleneck-how-gpu-memory-hierarchy-affects-your-computing-experience)
* [AI and Memory Wall](https://arxiv.org/abs/2403.14123)
* [Generative AI on the Edge: Architecture and Performance Evaluation](https://arxiv.org/html/2411.17712v1)
* [LLM-Inference-Bench: Inference Benchmarking of Large Language Models on AI Accelerators](https://arxiv.org/pdf/2411.00136)
* [A Comprehensive Guide to Understanding AI Inference on the CPU](https://armkeil.blob.core.windows.net/developer/Files/pdf/ebook/arm-guide-to-ai-inference-on-cpus-2024.pdf)
* [10 Key Decision Points for Generative AI Success](https://www2.deloitte.com/content/dam/Deloitte/uk/Documents/consultancy/deloitte-uk-genai-ido-pov-final-new.pdf)

## Datasets 

* [Awesome JSON Datasets](https://github.com/jdorfman/awesome-json-datasets?tab=readme-ov-file)
* [CORGIS JSON Datasets](https://corgis-edu.github.io/corgis/json/)
* [Kaggle JSON Datasets](https://www.kaggle.com/datasets?fileType=json)
* [data.nasa.gov](https://data.nasa.gov/)

## on Agents and Frameworks

* [What are AI Agents](https://spectrum.ieee.org/ai-agents)
* [Demystifying the Concept of Agentic AI Frameworks](https://medium.com/@soumava.dey.aig/demystifying-the-concept-of-agentic-ai-frameworks-6f86d1963039)
* [Discover the Hidden Potential of AI Agents in Everyday Life](https://www.geeky-gadgets.com/ai-agents-tools-and-frameworks/)
* [The AI Agents Stack](https://www.letta.com/blog/ai-agents-stack)
* [How AI agents are revolutionizing digital marketing](https://searchengineland.com/ai-agents-digital-marketing-448342)
* [Microsoft’s Agentic AI Frameworks: AutoGen and Semantic Kernel](https://devblogs.microsoft.com/semantic-kernel/microsofts-agentic-ai-frameworks-autogen-and-semantic-kernel/)
