# =============================================================================
# Personal Finance Tracker - EC2 Configuration
# EC2 Instance, Security Group, User Data (Java 17 + systemd service)
# =============================================================================

# -----------------------------------------------------------------------------
# EC2 Security Group
# -----------------------------------------------------------------------------
resource "aws_security_group" "ec2" {
  name        = "finance-tracker-ec2-sg"
  description = "Security group for Finance Tracker backend EC2 instance"
  vpc_id      = aws_vpc.main.id

  # SSH access
  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTP access
  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTPS access
  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Spring Boot application port
  ingress {
    description = "Spring Boot"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow all outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "finance-tracker-ec2-sg"
    Project = "personal-finance-tracker"
  }
}

# -----------------------------------------------------------------------------
# EC2 Instance
# -----------------------------------------------------------------------------
resource "aws_instance" "backend" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  key_name               = var.key_pair_name
  subnet_id              = aws_subnet.public_a.id
  vpc_security_group_ids = [aws_security_group.ec2.id]

  user_data = <<-EOF
    #!/bin/bash
    set -e

    # Update system packages
    yum update -y

    # Install Java 17 (Amazon Corretto)
    yum install -y java-17-amazon-corretto-headless

    # Verify Java installation
    java -version

    # Create application directory
    mkdir -p /opt/finance-tracker
    chown ec2-user:ec2-user /opt/finance-tracker

    # Create systemd service for the Finance Tracker application
    cat > /etc/systemd/system/finance-tracker.service <<'SERVICE'
    [Unit]
    Description=Personal Finance Tracker Spring Boot Application
    After=network.target

    [Service]
    Type=simple
    User=ec2-user
    WorkingDirectory=/opt/finance-tracker
    ExecStart=/usr/bin/java -jar /opt/finance-tracker/app.jar --spring.profiles.active=prod
    Restart=on-failure
    RestartSec=10
    StandardOutput=journal
    StandardError=journal
    SyslogIdentifier=finance-tracker

    # Environment variables for database connection
    Environment=SPRING_DATASOURCE_URL=jdbc:mysql://${aws_db_instance.mysql.endpoint}/personal_finance_tracker
    Environment=SPRING_DATASOURCE_USERNAME=${var.db_username}
    Environment=SPRING_DATASOURCE_PASSWORD=${var.db_password}

    [Install]
    WantedBy=multi-user.target
    SERVICE

    # Reload systemd and enable the service
    systemctl daemon-reload
    systemctl enable finance-tracker

    echo "EC2 user data setup completed successfully"
  EOF

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  tags = {
    Name    = "finance-tracker-backend"
    Project = "personal-finance-tracker"
  }
}

# -----------------------------------------------------------------------------
# Elastic IP for EC2 Instance (static public IP)
# -----------------------------------------------------------------------------
resource "aws_eip" "backend" {
  instance = aws_instance.backend.id
  domain   = "vpc"

  tags = {
    Name    = "finance-tracker-backend-eip"
    Project = "personal-finance-tracker"
  }
}
