# Key Pair
module "ollama_key_pair" {
  source                = "git::https://github.com/cloudposse/terraform-aws-key-pair.git?ref=main"
  namespace             = "ollama"
  stage                 = "server"
  name                  = var.key_name
  ssh_public_key_path   = pathexpand("~/.ssh")
  generate_ssh_key      = "true"
  private_key_extension = ".pem"
  public_key_extension  = ".pub"
}

# VPC Resources
resource "aws_vpc" "ollama_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "ollama-vpc"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "ollama_igw" {
  vpc_id = aws_vpc.ollama_vpc.id

  tags = {
    Name = "ollama-igw"
  }
}

# Subnet
resource "aws_subnet" "ollama_subnet" {
  vpc_id                  = aws_vpc.ollama_vpc.id
  cidr_block              = "10.0.1.0/24"
  map_public_ip_on_launch = true
  availability_zone       = data.aws_availability_zones.available.names[0]

  tags = {
    Name = "ollama-subnet"
  }
}

# Route Table
resource "aws_route_table" "ollama_route_table" {
  vpc_id = aws_vpc.ollama_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.ollama_igw.id
  }

  tags = {
    Name = "ollama-route-table"
  }
}

# Route Table Association
resource "aws_route_table_association" "ollama_route_table_assoc" {
  subnet_id      = aws_subnet.ollama_subnet.id
  route_table_id = aws_route_table.ollama_route_table.id
}

# Security Group
resource "aws_security_group" "ollama_sg" {
  name        = "ollama-security-group"
  description = "Security group for Ollama server"
  vpc_id      = aws_vpc.ollama_vpc.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 11434
    to_port     = 11434
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "ollama-security-group"
  }
}

# Data source for latest Ubuntu AMI
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# Data source for availability zones
data "aws_availability_zones" "available" {
  state = "available"
}

# EC2 Instance
resource "aws_instance" "ollama_instance" {
  ami           = data.aws_ami.ubuntu.id
  instance_type = var.use_gpu ? var.gpu_instance_type : var.instance_type

  subnet_id                   = aws_subnet.ollama_subnet.id
  vpc_security_group_ids      = [aws_security_group.ollama_sg.id]
  key_name                    = module.ollama_key_pair.key_name
  associate_public_ip_address = true

  root_block_device {
    volume_size = var.volume_size
  }

  user_data = templatefile("${path.module}/user_data.tpl", {
    use_gpu = var.use_gpu
  })

  tags = {
    Name = var.instance_name
  }
}
