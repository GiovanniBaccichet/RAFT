export DEBIAN_FRONTEND=noninteractive

# Installing OpenVSwitch

apt-get update
apt-get install -y tcpdump
apt-get install -y openvswitch-common openvswitch-switch apt-transport-https ca-certificates curl software-properties-common

# Startup commands for switch go here

sudo ovs-vsctl add-br vSwitchRouter
sudo ovs-vsctl add-br vSwitchPart1
sudo ovs-vsctl add-br vSwitchPart2

# Connect bridges together

sudo ovs-vsctl \
-- add-port vSwitchRouter patch1 \
-- set interface patch1 type=patch options:peer=patch3 \
-- add-port vSwitchPart1 patch3 \
-- set interface patch3 type=patch options:peer=patch1

sudo ovs-vsctl \
-- add-port vSwitchRouter patch2 \
-- set interface patch2 type=patch options:peer=patch4 \
-- add-port vSwitchPart2 patch4 \
-- set interface patch4 type=patch options:peer=patch2

sudo ovs-vsctl add-port vSwitchRouter enp0s8 # Router
sudo ovs-vsctl add-port vSwitchPart1 enp0s9 # Node 1
sudo ovs-vsctl add-port vSwitchPart1 enp0s10 # Node 2
sudo ovs-vsctl add-port vSwitchPart2 enp0s16 # Node 3
sudo ovs-vsctl add-port vSwitchPart2 enp0s17 # Node 4
sudo ovs-vsctl add-port vSwitchPart2 enp0s18 # Node 5

sudo ip link set dev enp0s8 up # Router
sudo ip link set dev enp0s9 up # Node 1
sudo ip link set dev enp0s10 up # Node 2
sudo ip link set dev enp0s16 up # Node 3
sudo ip link set dev enp0s17 up # Node 4
sudo ip link set dev enp0s18 up # Node 5