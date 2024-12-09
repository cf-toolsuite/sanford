# Outputs
output "instance_id" {
  value = google_compute_instance.ollama_instance.id
}

output "public_ip" {
  value = google_compute_instance.ollama_instance.network_interface[0].access_config[0].nat_ip
}
