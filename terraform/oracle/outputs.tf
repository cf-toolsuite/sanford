output "instance_ocid" {
  value       = oci_core_instance.ollama_instance.id
  description = "The OCID of the Ollama instance."
}

output "public_ip" {
  value = oci_core_instance.ollama_instance.public_ip
  description = "The public IP address of the Ollama instance."
}