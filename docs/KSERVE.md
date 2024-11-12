# Sanford

## How to setup a dedicated cluster for LLM inference

Create another Kind cluster

```bash
kind create cluster --name kserve-cluster
```

Install [KServe](https://kserve.github.io/website/latest/)

```bash
curl -s "https://raw.githubusercontent.com/kserve/kserve/release-0.13/hack/quick_install.sh" | bash
```

Wait for all pods to be in the "Running" state:

```bash
kubectl get pods -A
```

Create a Namespace

```bash
kubectl create namespace llm-demo
```

Create an InferenceService manifest to deploy a [Hugging Face model](https://huggingface.co/models) using [OCI ModelContainer](https://kserve.github.io/website/master/modelserving/storage/oci/#prepare-an-oci-image-with-model-data).

Here's a sample YAML:

```yaml
apiVersion: "serving.kserve.io/v1beta1"
kind: "InferenceService"
metadata:
  name: "hf-llm"
  namespace: "llm-demo"
spec:
  predictor:
    model:
      modelFormat:
        name: pytorch
      runtime: kserve-ocicontainer
      storageUri: oci://ghcr.io/my-repo/my-hf-model:latest
      resources:
        limits:
          cpu: "4"
          memory: 8Gi
          nvidia.com/gpu: "1"
        requests:
          cpu: "1"
          memory: 4Gi
```

> Remember to replace `ghcr.io/my-repo/my-hf-model:latest` in the YAML above with the actual OCI image containing your Hugging Face model. You'll need to build and push this image to a container registry accessible by your Kind cluster.

Save this YAML to a file named `hf-llm-inferenceservice.yml`.

Apply the InferenceService manifest:

```bash
kubectl apply -f hf-llm-inferenceservice.yaml
```

Monitor the status of your InferenceService:

```bash
kubectl get inferenceservice hf-llm -n llm-demo
```

Retrieve the URL for your InferenceService:

```bash
kubectl get inferenceservice hf-llm -n llm-demo -o jsonpath='{.status.url}'
```
