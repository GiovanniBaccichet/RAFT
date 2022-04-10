export DEBIAN_FRONTEND=noninteractive
# Startup commands go here

sudo apt-get update
sudo apt-get install net-tools

# Networking
sudo ip addr add 10.0.0.15/24 dev enp0s8
sudo ip link set dev enp0s8 up

sudo ip route add 10.0.1.0/24 via 10.0.0.5