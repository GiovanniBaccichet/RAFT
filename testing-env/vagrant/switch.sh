export DEBIAN_FRONTEND=noninteractive

# Installing OpenVSwitch

apt-get update
apt-get install -y tcpdump
apt-get install -y openvswitch-common openvswitch-switch apt-transport-https ca-certificates curl software-properties-common

# Startup commands for switch go here

sudo ovs-vsctl add-br switch

sudo ovs-vsctl add-port switch enp0s8 # Router
sudo ovs-vsctl add-port switch enp0s9 # Node 1
sudo ovs-vsctl add-port switch enp0s10 # Node 2
sudo ovs-vsctl add-port switch enp0s16 # Node 3
sudo ovs-vsctl add-port switch enp0s17 # Node 4
sudo ovs-vsctl add-port switch enp0s18 # Node 5

sudo ip link set dev enp0s8 up # Router
sudo ip link set dev enp0s9 up # Node 1
sudo ip link set dev enp0s10 up # Node 2
sudo ip link set dev enp0s16 up # Node 3
sudo ip link set dev enp0s17 up # Node 4
sudo ip link set dev enp0s18 up # Node 5