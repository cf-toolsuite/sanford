resource "oci_core_instance" "ollama_instance" {
  ssh_public_keys = [var.public_key] # Assumes you'll provide the public key
}


# Virtual Cloud Network (VCN)
resource "oci_core_vcn" "ollama_vcn" {
  cidr_block = "10.0.0.0/16"
  compartment_id = var.compartment_ocid # Required for all Oracle resources
  display_name = "ollama-vcn"
  dns_label = "ollama-vcn" # Optional
}

# Internet Gateway
resource "oci_core_internet_gateway" "ollama_igw" {
  compartment_id = var.compartment_ocid
  display_name = "ollama-igw"
  vcn_id = oci_core_vcn.ollama_vcn.id
}

# Subnet
resource "oci_core_subnet" "ollama_subnet" {
  cidr_block = "10.0.1.0/24"
  compartment_id = var.compartment_ocid
  display_name = "ollama-subnet"
  vcn_id = oci_core_vcn.ollama_vcn.id
  prohibit_public_ip_on_vnic = false # Allow public IPs
  availability_domain = data.oci_identity_availability_domains.ads.availability_domains[0].name
}

# Route Table
resource "oci_core_route_table" "ollama_route_table" {
  compartment_id = var.compartment_ocid
  display_name = "ollama-route-table"
  vcn_id = oci_core_vcn.ollama_vcn.id

  route_rules {
    destination = "0.0.0.0/0"
    network_entity_id = oci_core_internet_gateway.ollama_igw.id
  }
}

# Route Table Association
resource "oci_core_route_table_attachment" "ollama_route_table_assoc" {
  subnet_id = oci_core_subnet.ollama_subnet.id
  route_table_id = oci_core_route_table.ollama_route_table.id
}

# Security List (similar to Security Group)
resource "oci_core_security_list" "ollama_sl" {
  compartment_id = var.compartment_ocid
  display_name = "ollama-security-list"
  vcn_id = oci_core_vcn.ollama_vcn.id

 ingress_security_rules {
    protocol = "6" # TCP
    source = "0.0.0.0/0"
    tcp_options {
      destination_port_range {
        min = 22
        max = 22
      }
    }
 }

 ingress_security_rules {
    protocol = "6" # TCP
    source = "0.0.0.0/0"
    tcp_options {
      destination_port_range {
        min = 11434
        max = 11434
      }
    }
 }

  egress_security_rules {
    protocol = "all" # Allow all outbound traffic
    destination = "0.0.0.0/0"
  }
}


# Instance
resource "oci_core_instance" "ollama_instance" {
  availability_domain = data.oci_identity_availability_domains.ads.availability_domains[0].name
  compartment_id = var.compartment_ocid
  display_name = var.instance_name
  shape = var.instance_shape # Use shapes instead of instance types
  subnet_id = oci_core_subnet.ollama_subnet.id

  create_vnic_details {
    assign_public_ip = true
    subnet_id = oci_core_subnet.ollama_subnet.id
    security_list_ids = [oci_core_security_list.ollama_sl.id]
  }
  source_details {
    source_type = "image"
    image_id = data.oci_core_images.ollama_image.id
  }

  user_data = base64encode(templatefile("${path.module}/user_data.tpl", {
    use_gpu = var.use_gpu
  }))
}


data "oci_core_images" "ollama_image" {
  compartment_id = var.compartment_ocid
  operating_system = "Ubuntu"
  shape = var.instance_shape # Make sure the image is compatible with the shape
  sort_by = "TIMECREATED"
  sort_order = "DESC" # Get the most recent image
  filter {
    name = "display_name"
    values = ["Canonical Ubuntu Server 22.04 LTS AMD Based VM"]
  }
}

data "oci_identity_availability_domains" "ads" {
  compartment_id = var.compartment_ocid
}