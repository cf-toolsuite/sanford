# Configure the Google Cloud provider
provider "google" {
  project = var.project_id
  region  = var.region
}
