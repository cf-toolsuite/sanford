# VPC Network
resource "google_compute_network" "ollama_vpc" {
  name                    = "ollama-vpc"
  auto_create_subnetworks = false
}

# Subnet
resource "google_compute_subnetwork" "ollama_subnet" {
  name          = "ollama-subnet"
  ip_cidr_range = "10.0.1.0/24"
  network       = google_compute_network.ollama_vpc.id
  region        = var.region
}

# Firewall rules
resource "google_compute_firewall" "ollama_firewall" {
  name    = "ollama-firewall"
  network = google_compute_network.ollama_vpc.name

  allow {
    protocol = "tcp"
    ports    = ["22", "11434"]
  }

  source_ranges = ["0.0.0.0/0"]
}

# Service account
data "google_service_account" "ollama_sa" {
  account_id   = var.service_account
}

# Compute Instance
resource "google_compute_instance" "ollama_instance" {
  name         = var.instance_name
  machine_type = var.use_gpu ? var.gpu_machine_type : var.machine_type
  zone         = var.zone

  boot_disk {
    initialize_params {
      image = "ubuntu-os-cloud/ubuntu-2204-lts"
      size  = var.volume_size
    }
  }

  network_interface {
    network    = google_compute_network.ollama_vpc.name
    subnetwork = google_compute_subnetwork.ollama_subnet.name
    access_config {
      // Ephemeral public IP
    }
  }

  service_account {
    email  = data.google_service_account.ollama_sa.email
    scopes = ["cloud-platform"]
  }

  metadata_startup_script = templatefile("${path.module}/user_data.tpl", {
    use_gpu = var.use_gpu
  })

  dynamic "guest_accelerator" {
    for_each = var.use_gpu ? [1] : []
    content {
      type  = var.gpu_type
      count = var.gpu_count
    }
  }

  scheduling {
    on_host_maintenance = var.use_gpu ? "TERMINATE" : "MIGRATE"
  }
}
