# Variables
variable "region" {
  description = "AWS region"
  type        = string
  default     = "us-west-2"
}

variable "instance_name" {
  description = "Name tag for the EC2 instance"
  type        = string
  default     = "ollama-instance"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "m6i.4xlarge"
}

variable "gpu_instance_type" {
  description = "GPU EC2 instance type"
  type        = string
  default     = "p3.8xlarge"
}

variable "use_gpu" {
  description = "Whether to use GPU instance"
  type        = bool
  default     = false
}

variable "volume_size" {
  description = "Size of the root volume in GB"
  type        = number
  default     = 80
}

variable "key_name" {
  description = "Name of the SSH key pair"
  type        = string
  default     = "ssh-key"
}
