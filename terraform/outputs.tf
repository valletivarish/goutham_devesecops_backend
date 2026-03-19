# =============================================================================
# Personal Finance Tracker - Terraform Outputs
# =============================================================================

output "ec2_public_ip" {
  description = "Elastic IP address of the backend EC2 instance"
  value       = aws_eip.backend.public_ip
}

output "s3_website_url" {
  description = "S3 static website URL for the frontend"
  value       = aws_s3_bucket_website_configuration.frontend.website_endpoint
}

output "rds_endpoint" {
  description = "RDS MySQL instance endpoint (host:port)"
  value       = aws_db_instance.mysql.endpoint
}

output "s3_bucket_name" {
  description = "Name of the S3 bucket hosting the frontend"
  value       = aws_s3_bucket.frontend.bucket
}

output "vpc_id" {
  description = "ID of the VPC"
  value       = aws_vpc.main.id
}
