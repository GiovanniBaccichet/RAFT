export DEBIAN_FRONTEND=noninteractive

# Installing OpenVSwitch

apt-get update
apt-get install -y tcpdump
apt-get install -y openvswitch-common openvswitch-switch apt-transport-https ca-certificates curl software-properties-common net-tools

# Startup commands for switch go here

sudo ovs-vsctl add-br vSwitch

# Create trunk ports on the bridge

sudo ovs-vsctl add-port vSwitch enp0s8 # Router -> switch
sudo ovs-vsctl add-port vSwitch enp0s9 # Router -> node-1
# sudo ovs-vsctl add-port vSwitch enp0s10 # Router -> node-2
# sudo ovs-vsctl add-port vSwitch enp0s16 # Router -> node-3
# sudo ovs-vsctl add-port vSwitch enp0s17 # Router -> node-4
# sudo ovs-vsctl add-port vSwitch enp0s18 # Router -> node-5

sudo ip link set dev enp0s8 up
sudo ip link set dev enp0s9 up
# sudo ip link set dev enp0s10 up
# sudo ip link set dev enp0s16 up
# sudo ip link set dev enp0s17 up
# sudo ip link set dev enp0s18 up