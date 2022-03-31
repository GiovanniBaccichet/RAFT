# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure("2") do |config|
  config.vm.box_check_update = false
  config.ssh.forward_agent = true
  config.ssh.forward_x11 = true
  config.vm.provider "virtualbox" do |vb|
    vb.customize ["modifyvm", :id, "--usb", "on"]
    vb.customize ["modifyvm", :id, "--usbehci", "off"]
    vb.customize ["modifyvm", :id, "--nicpromisc2", "allow-all"]
    vb.customize ["modifyvm", :id, "--nicpromisc3", "allow-all"]
    vb.customize ["modifyvm", :id, "--nicpromisc4", "allow-all"]
    vb.customize ["modifyvm", :id, "--nicpromisc5", "allow-all"]
    vb.customize ["modifyvm", :id, "--nicpromisc6", "allow-all"]
    vb.customize ["modifyvm", :id, "--nicpromisc7", "allow-all"]
    vb.customize ["modifyvm", :id, "--nicpromisc8", "allow-all"]
    vb.customize ["modifyvm", :id, "--nicpromisc9", "allow-all"]
    vb.cpus = 2
  end

  # CLIENT

  config.vm.define "client" do |client|
    client.vm.box = "ubuntu/focal64"
    client.vm.hostname = "client"
    client.vm.network "private_network", virtualbox__intnet: "broadcast_router-client", ip: "10.0.0.100"
    client.vm.provision "shell", path: "vagrant/client.sh"
    client.vm.provider "virtualbox" do |vb|
      vb.memory = 512
    end
  end

  # ROUTER

  config.vm.define "router" do |router|
    router.vm.box = "ubuntu/focal64"
    router.vm.hostname = "router"
    router.vm.network "private_network", virtualbox__intnet: "broadcast_router-client"
    router.vm.network "private_network", virtualbox__intnet: "broadcast_router-switch"
    router.vm.provision "shell", path: "vagrant/router.sh"
    router.vm.provider "virtualbox" do |vb|
      vb.memory = 256
    end
  end

  # SWITCH

  config.vm.define "switch" do |switch|
    switch.vm.box = "ubuntu/focal64"
    switch.vm.hostname = "switch"
    switch.vm.network "private_network", virtualbox__intnet: "broadcast_router-switch"
    switch.vm.network "private_network", virtualbox__intnet: "broadcast_router-node1"
    switch.vm.network "private_network", virtualbox__intnet: "broadcast_router-node2"
    switch.vm.network "private_network", virtualbox__intnet: "broadcast_router-node3"
    switch.vm.network "private_network", virtualbox__intnet: "broadcast_router-node4"
    switch.vm.network "private_network", virtualbox__intnet: "broadcast_router-node5"
    switch.vm.provision "shell", path: "vagrant/switch.sh"
    switch.vm.provider "virtualbox" do |vb|
      vb.memory = 256
    end
  end

  # NODE N.1

  config.vm.define "node1" do |node1|
    node1.vm.box = "ubuntu/focal64"
    node1.vm.hostname = "node1"
    node1.vm.network "private_network", virtualbox__intnet: "broadcast_router-node1", ip: "10.0.0.1"
    node1.vm.provision "shell", path: "vagrant/node.sh"
    node1.vm.provider "virtualbox" do |vb|
      vb.memory = 512
    end
  end

  # NODE N.2

  config.vm.define "node2" do |node2|
    node2.vm.box = "ubuntu/focal64"
    node2.vm.hostname = "node2"
    node2.vm.network "private_network", virtualbox__intnet: "broadcast_router-node2", ip: "10.0.0.2"
    node2.vm.provision "shell", path: "vagrant/node.sh"
    node2.vm.provider "virtualbox" do |vb|
      vb.memory = 512
    end
  end

  # NODE N.3

  config.vm.define "node3" do |node3|
    node3.vm.box = "ubuntu/focal64"
    node3.vm.hostname = "node3"
    node3.vm.network "private_network", virtualbox__intnet: "broadcast_router-node3", ip: "10.0.0.3"
    node3.vm.provision "shell", path: "vagrant/node.sh"
    node3.vm.provider "virtualbox" do |vb|
      vb.memory = 512
    end
  end

  # NODE N.4

  config.vm.define "node4" do |node4|
    node4.vm.box = "ubuntu/focal64"
    node4.vm.hostname = "node4"
    node4.vm.network "private_network", virtualbox__intnet: "broadcast_router-node4", ip: "10.0.0.4"
    node4.vm.provision "shell", path: "vagrant/node.sh"
    node4.vm.provider "virtualbox" do |vb|
      vb.memory = 512
    end
  end

  # NODE N.5

  config.vm.define "node5" do |node5|
    node5.vm.box = "ubuntu/focal64"
    node5.vm.hostname = "node5"
    node5.vm.network "private_network", virtualbox__intnet: "broadcast_router-node5", ip: "10.0.0.5"
    node5.vm.provision "shell", path: "vagrant/node.sh"
    node5.vm.provider "virtualbox" do |vb|
      vb.memory = 512
    end
  end

end
