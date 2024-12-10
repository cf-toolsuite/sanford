# Outputs
output "instance_id" {
  value = aws_instance.ollama_instance.id
}

output "public_ip" {
  value = aws_instance.ollama_instance.public_ip
}

output "key_name" {
  value = module.ollama_key_pair.key_name
}
