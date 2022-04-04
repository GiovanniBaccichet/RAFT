export DEBIAN_FRONTEND=noninteractive
# Startup commands go here

sudo ip addr add 10.0.0.2/27 dev enp0s8
sudo ip link set dev enp0s8 up

sudo ip route add 10.0.1.0/27 via 10.0.0.1
