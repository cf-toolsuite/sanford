# Variables
variable "project_id" {
  description = "Google Cloud Project ID"
  type        = string
}

variable "region" {
  description = "GCP region"
  type        = string
  default     = "us-west2"
}

variable "zone" {
  description = "GCP zone"
  type        = string
  default     = "us-west2-b"
}

variable "instance_name" {
  description = "Name for the VM instance"
  type        = string
  default     = "ollama-instance"
}

variable "machine_type" {
  description = "Machine type for the VM"
  type        = string
  default     = "c2d-highmem-8"
}

variable "gpu_machine_type" {
  description = "GPU machine type"
  type        = string
  default     = "n1-highmem-8"
}

variable "use_gpu" {
  description = "Whether to use GPU instance"
  type        = bool
  default     = false
}

variable "gpu_type" {
  description = "Type of GPU to attach"
  type        = string
  default     = "nvidia-tesla-p4"
}

variable "gpu_count" {
  description = "Number of GPUs to attach"
  type        = number
  default     = 1
}

variable "volume_size" {
  description = "Size of the boot disk in GB"
  type        = number
  default     = 80
}

variable "service_account" {
  description = "The email address of an existing service account"
  type        = string
}
