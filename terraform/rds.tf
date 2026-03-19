# =============================================================================
# Personal Finance Tracker - RDS Configuration
# MySQL 8.0 Instance in Private Subnet
# =============================================================================

# -----------------------------------------------------------------------------
# RDS Subnet Group (private subnets)
# -----------------------------------------------------------------------------
resource "aws_db_subnet_group" "mysql" {
  name       = "finance-tracker-db-subnet-group"
  subnet_ids = [aws_subnet.private_a.id, aws_subnet.private_b.id]

  tags = {
    Name    = "finance-tracker-db-subnet-group"
    Project = "personal-finance-tracker"
  }
}

# -----------------------------------------------------------------------------
# RDS Security Group
# -----------------------------------------------------------------------------
resource "aws_security_group" "rds" {
  name        = "finance-tracker-rds-sg"
  description = "Security group for Finance Tracker RDS MySQL instance"
  vpc_id      = aws_vpc.main.id

  # Allow MySQL traffic only from the EC2 security group
  ingress {
    description     = "MySQL from EC2"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2.id]
  }

  # Allow all outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "finance-tracker-rds-sg"
    Project = "personal-finance-tracker"
  }
}

# -----------------------------------------------------------------------------
# RDS MySQL Instance
# -----------------------------------------------------------------------------
resource "aws_db_instance" "mysql" {
  identifier     = "finance-tracker-db"
  engine         = "mysql"
  engine_version = "8.0"
  instance_class = "db.t3.micro"

  allocated_storage     = 20
  max_allocated_storage = 50
  storage_type          = "gp3"

  db_name  = "personal_finance_tracker"
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.mysql.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false

  backup_retention_period = 0
  skip_final_snapshot     = true
  deletion_protection     = false

  multi_az = false

  tags = {
    Name    = "finance-tracker-db"
    Project = "personal-finance-tracker"
  }
}
