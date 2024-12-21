terraform {
  required_providers {
    oci = {
      source  = "oracle/oci"
      version = "~> 4.50.0"
    }
  }
}

provider "oci" {
  region = var.region
  tenancy_ocid = var.tenancy_ocid
}

# Set Environment Variables
#
# You'll need to set the following environment variables:
#
# TF_VAR_tenancy_ocid: Your tenancy OCID.
# TF_VAR_user_ocid: Your user OCID.
# TF_VAR_fingerprint: The fingerprint of your API signing key.
# TF_VAR_private_key_path: The path to your private API signing key.
# TF_VAR_region: The region you want to deploy to.
