variable "compartment_ocid" {
  type = string
  description = "The OCID of the compartment where resources will be created."
}

variable "instance_name" {
  type = string
  description = "The name of the instance."
  default = "ollama-instance"
}

variable "instance_shape" {
  type = string
  description = "The shape of the instance (e.g., 'VM.Standard2.1')."
}


variable "public_key" {
  type = string
  description = "The public SSH key for accessing the instance."
}


variable "volume_size" {
  type = number
  description = "Size of the boot volume in GB."
  default = 50
}

variable "use_gpu" {
  type = bool
  description = "Whether to install GPU dependencies (true/false)."
  default = false
}

variable "region" {
  type = string
  description = "The Oracle Cloud region to deploy to (e.g., 'us-ashburn-1')."
}

variable "tenancy_ocid" {
  type    = string
  description = "The OCID of your tenancy."
}
