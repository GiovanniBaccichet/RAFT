export DEBIAN_FRONTEND=noninteractive
# Startup commands go here

# Client LAN interface
sudo ip addr add 10.0.0.1/27 dev enp0s8
sudo ip link set dev enp0s8 up

# Switch interface
sudo ip addr add 10.0.1.1/27 dev enp0s9
sudo ip link set dev enp0s9 up

# IPv4 forwarding
sysctl -w net.ipv4.ip_forward=1