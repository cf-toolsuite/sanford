variable "resource_group" {
  type    = string
  default = "ollamatestbed"
}

variable "location" {
  type    = string
  default = "West US 2"
}

variable "vm_name" {
  type    = string
  default = "ollamaX01"
}

variable "vm_size" {
  type    = string
  default = "Standard_D16s_v4"
}

variable "gpu_vm_size" {
  type    = string
  default = "Standard_NC12s_v3"
}

variable "use_gpu" {
  type    = bool
  default = false
}

variable "disk_size" {
  type    = number
  default = 80
}

variable "admin_username" {
  type    = string
  default = "aipro"
}
