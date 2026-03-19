# =============================================================================
# Personal Finance Tracker - Terraform Variables
# =============================================================================

variable "aws_region" {
  description = "AWS region for all resources"
  type        = string
  default     = "us-east-1"
}

variable "key_pair_name" {
  description = "Name of the AWS key pair for SSH access to EC2"
  type        = string
}

variable "db_username" {
  description = "Master username for the RDS MySQL instance"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Master password for the RDS MySQL instance"
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.db_password) >= 8
    error_message = "Database password must be at least 8 characters long."
  }
}

variable "instance_type" {
  description = "EC2 instance type for the backend server"
  type        = string
  default     = "t3.micro"
}

variable "ami_id" {
  description = "AMI ID for the EC2 instance (Amazon Linux 2023)"
  type        = string
  default     = "ami-0c02fb55956c7d316"
}
