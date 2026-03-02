# =============================================================================
# Personal Finance Tracker - S3 Configuration
# S3 Bucket for Frontend Static Website Hosting
# =============================================================================

# -----------------------------------------------------------------------------
# S3 Bucket
# -----------------------------------------------------------------------------
resource "aws_s3_bucket" "frontend" {
  bucket = "finance-tracker-frontend-${random_id.bucket_suffix.hex}"

  tags = {
    Name    = "finance-tracker-frontend"
    Project = "personal-finance-tracker"
  }
}

# Generate a random suffix to ensure bucket name uniqueness
resource "random_id" "bucket_suffix" {
  byte_length = 4
}

# -----------------------------------------------------------------------------
# S3 Bucket Website Configuration
# -----------------------------------------------------------------------------
resource "aws_s3_bucket_website_configuration" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  index_document {
    suffix = "index.html"
  }

  error_document {
    key = "index.html"
  }
}

# -----------------------------------------------------------------------------
# S3 Bucket Public Access Settings
# -----------------------------------------------------------------------------
resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

# -----------------------------------------------------------------------------
# S3 Bucket Policy - Public Read Access
# -----------------------------------------------------------------------------
resource "aws_s3_bucket_policy" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  depends_on = [aws_s3_bucket_public_access_block.frontend]

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "PublicReadGetObject"
        Effect    = "Allow"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource  = "${aws_s3_bucket.frontend.arn}/*"
      }
    ]
  })
}
