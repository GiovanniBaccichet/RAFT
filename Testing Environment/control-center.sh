#!/bin/bash
# utilitymenu.sh - A sample shell script to display menus on screen
# Store menu options selected by the user
INPUT=/tmp/menu.sh.$$

# Storage file for displaying cal and date command output
OUTPUT=/tmp/output.sh.$$

# trap and delete temp files
trap "rm $OUTPUT; rm $INPUT; exit" SIGHUP SIGINT SIGTERM

#
# Purpose - display output using msgbox 
#  $1 -> set msgbox height
#  $2 -> set msgbox width
#  $3 -> set msgbox title
#
function display_output(){
	local h=${1-10}			# box height default 10
	local w=${2-41} 		# box width default 41
	local t=${3-Output} 	# box title 
	dialog --backtitle "Raft Consensus Algorithm Testbed" --title "${t}" --clear --msgbox "$(<$OUTPUT)" ${h} ${w}
}



function sys_util(){
	dialog --clear --backtitle "Raft Consensus Algorithm Testbed" \
--title "[ S Y S - U T I L I T Y ]" \
--menu "Manage the VMs:" 15 80 8 \
Startup "Start the testbed" \
Shut\ Down "Halt the testbed" 2>"${INPUT}"

sys_menuitem=$(<"${INPUT}")

# make decsion 
case $sys_menuitem in
	Startup) vagrant up;;
	Shut\ Down) vagrant halt;;
	Exit) echo "Bye"; break;;
esac

}

#########################################
# 			DELAY SIMULATION			#
#########################################

function delay_menu(){
	dialog --clear --backtitle "Raft Consensus Algorithm Testbed" \
--title "[ D E L A Y - S I M U L A T I O N ]" \
--menu "Simulate delay on links:" 20 80 8 \
Break\ Link\ 1 "Simulate delay on link vSwitchPart1 -> Node 1" \
Clean\ Link\ 1 "Remove the rules applied to link vSwitchPart1 -> Node 1" \
Break\ Link\ 2 "Simulate delay on link vSwitchPart1 -> Node 2" \
Clean\ Link\ 2 "Remove the rules applied to link vSwitchPart1 -> Node 2" \
Break\ Link\ 3 "Simulate delay on link vSwitchPart2 -> Node 3" \
Clean\ Link\ 3 "Remove the rules applied to link vSwitchPart2 -> Node 3" \
Break\ Link\ 4 "Simulate delay on link vSwitchPart2 -> Node 4" \
Clean\ Link\ 4 "Remove the rules applied to link vSwitchPart2 -> Node 4" \
Break\ Link\ 5 "Simulate delay on link vSwitchPart2 -> Node 5" \
Clean\ Link\ 5 "Remove the rules applied to link vSwitchPart2 -> Node 5" 2>"${INPUT}"

delay_menuitem=$(<"${INPUT}")

# make decsion 
case $delay_menuitem in
	Break\ Link\ 1) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s9 root netem delay 1000ms 20ms distribution normal';;
	Clean\ Link\ 1) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s9 root';;
	Break\ Link\ 2) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s10 root netem delay 1000ms 20ms distribution normal';;
	Clean\ Link\ 2) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s10 root';;
	Break\ Link\ 3) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s16 root netem delay 1000ms 20ms distribution normal';;
	Clean\ Link\ 3) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s16 root';;
	Break\ Link\ 4) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s17 root netem delay 1000ms 20ms distribution normal';;
	Clean\ Link\ 4) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s17 root';;
	Break\ Link\ 5) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s18 root netem delay 1000ms 20ms distribution normal';;
	Clean\ Link\ 5) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s18 root';;
	Exit) echo "Bye"; break;;
esac

}

#########################################
# 		PACKET LOSS SIMULATION			#
#########################################

function pktloss_menu(){
	dialog --clear --backtitle "Raft Consensus Algorithm Testbed" \
--title "[ P K T  L O S S - S I M U L A T I O N ]" \
--menu "Simulate delay on links:" 20 80 8 \
Break\ Link\ 1 "Simulate packet loss on link vSwitchPart1 -> Node 1" \
Clean\ Link\ 1 "Remove the rules applied to link vSwitchPart1 -> Node 1" \
Break\ Link\ 2 "Simulate packet loss on link vSwitchPart1 -> Node 2" \
Clean\ Link\ 2 "Remove the rules applied to link vSwitchPart1 -> Node 2" \
Break\ Link\ 3 "Simulate packet loss on link vSwitchPart2 -> Node 3" \
Clean\ Link\ 3 "Remove the rules applied to link vSwitchPart2 -> Node 3" \
Break\ Link\ 4 "Simulate packet loss on link vSwitchPart2 -> Node 4" \
Clean\ Link\ 4 "Remove the rules applied to link vSwitchPart2 -> Node 4" \
Break\ Link\ 5 "Simulate packet loss on link vSwitchPart2 -> Node 5" \
Clean\ Link\ 5 "Remove the rules applied to link vSwitchPart2 -> Node 5" 2>"${INPUT}"

pktloss_menuitem=$(<"${INPUT}")

# make decsion 
case $pktloss_menuitem in
	Break\ Link\ 1) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s9 root netem loss 10%';;
	Clean\ Link\ 1) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s9 root';;
	Break\ Link\ 2) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s10 root netem loss 10%';;
	Clean\ Link\ 2) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s10 root';;
	Break\ Link\ 3) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s16 root netem loss 10%';;
	Clean\ Link\ 3) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s16 root';;
	Break\ Link\ 4) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s17 root netem loss 10%';;
	Clean\ Link\ 4) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s17 root';;
	Break\ Link\ 5) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s18 root netem loss 10%';;
	Clean\ Link\ 5) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s18 root';;
	Exit) echo "Bye"; break;;
esac

}

#########################################
# 		PACKET LOSS SIMULATION			#
#########################################

function pktloss_menu(){
	dialog --clear --backtitle "Raft Consensus Algorithm Testbed" \
--title "[ P K T  L O S S - S I M U L A T I O N ]" \
--menu "Simulate packet loss on links:" 20 80 8 \
Break\ Link\ 1 "Simulate packet loss on link vSwitchPart1 -> Node 1" \
Clean\ Link\ 1 "Remove the rules applied to link vSwitchPart1 -> Node 1" \
Break\ Link\ 2 "Simulate packet loss on link vSwitchPart1 -> Node 2" \
Clean\ Link\ 2 "Remove the rules applied to link vSwitchPart1 -> Node 2" \
Break\ Link\ 3 "Simulate packet loss on link vSwitchPart2 -> Node 3" \
Clean\ Link\ 3 "Remove the rules applied to link vSwitchPart2 -> Node 3" \
Break\ Link\ 4 "Simulate packet loss on link vSwitchPart2 -> Node 4" \
Clean\ Link\ 4 "Remove the rules applied to link vSwitchPart2 -> Node 4" \
Break\ Link\ 5 "Simulate packet loss on link vSwitchPart2 -> Node 5" \
Clean\ Link\ 5 "Remove the rules applied to link vSwitchPart2 -> Node 5" 2>"${INPUT}"

pktloss_menuitem=$(<"${INPUT}")

# make decsion 
case $pktloss_menuitem in
	Break\ Link\ 1) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s9 root netem loss 10%';;
	Clean\ Link\ 1) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s9 root';;
	Break\ Link\ 2) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s10 root netem loss 10%';;
	Clean\ Link\ 2) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s10 root';;
	Break\ Link\ 3) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s16 root netem loss 10%';;
	Clean\ Link\ 3) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s16 root';;
	Break\ Link\ 4) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s17 root netem loss 10%';;
	Clean\ Link\ 4) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s17 root';;
	Break\ Link\ 5) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s18 root netem loss 10%';;
	Clean\ Link\ 5) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s18 root';;
	Exit) echo "Bye"; break;;
esac

}

#########################################
#  	  PACKET DUPLICATION SIMULATION		#
#########################################

function dup_menu(){
	dialog --clear --backtitle "Raft Consensus Algorithm Testbed" \
--title "[ P K T  D U P L. - S I M U L A T I O N ]" \
--menu "Simulate packet duplication on links:" 20 80 8 \
Break\ Link\ 1 "Simulate packet duplication on link vSwitchPart1 -> Node 1" \
Clean\ Link\ 1 "Remove the rules applied to link vSwitchPart1 -> Node 1" \
Break\ Link\ 2 "Simulate packet duplication on link vSwitchPart1 -> Node 2" \
Clean\ Link\ 2 "Remove the rules applied to link vSwitchPart1 -> Node 2" \
Break\ Link\ 3 "Simulate packet duplication on link vSwitchPart2 -> Node 3" \
Clean\ Link\ 3 "Remove the rules applied to link vSwitchPart2 -> Node 3" \
Break\ Link\ 4 "Simulate packet duplication on link vSwitchPart2 -> Node 4" \
Clean\ Link\ 4 "Remove the rules applied to link vSwitchPart2 -> Node 4" \
Break\ Link\ 5 "Simulate packet duplication on link vSwitchPart2 -> Node 5" \
Clean\ Link\ 5 "Remove the rules applied to link vSwitchPart2 -> Node 5" 2>"${INPUT}"

dup_menuitem=$(<"${INPUT}")

# make decsion 
case $dup_menuitem in
	Break\ Link\ 1) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s9 root netem duplicate 10%';;
	Clean\ Link\ 1) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s9 root';;
	Break\ Link\ 2) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s10 root netem duplicate 10%';;
	Clean\ Link\ 2) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s10 root';;
	Break\ Link\ 3) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s16 root netem duplicate 10%';;
	Clean\ Link\ 3) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s16 root';;
	Break\ Link\ 4) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s17 root netem duplicate 10%';;
	Clean\ Link\ 4) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s17 root';;
	Break\ Link\ 5) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s18 root netem duplicate 10%';;
	Clean\ Link\ 5) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s18 root';;
	Exit) echo "Bye"; break;;
esac

}

#########################################
#  	  PACKET CORRUPTION SIMULATION		#
#########################################

function corr_menu(){
	dialog --clear --backtitle "Raft Consensus Algorithm Testbed" \
--title "[ P K T  C O R R. - S I M U L A T I O N ]" \
--menu "Simulate delay on links:" 20 80 8 \
Break\ Link\ 1 "Simulate packet corruption on link vSwitchPart1 -> Node 1" \
Clean\ Link\ 1 "Remove the rules applied to link vSwitchPart1 -> Node 1" \
Break\ Link\ 2 "Simulate packet corruption on link vSwitchPart1 -> Node 2" \
Clean\ Link\ 2 "Remove the rules applied to link vSwitchPart1 -> Node 2" \
Break\ Link\ 3 "Simulate packet corruption on link vSwitchPart2 -> Node 3" \
Clean\ Link\ 3 "Remove the rules applied to link vSwitchPart2 -> Node 3" \
Break\ Link\ 4 "Simulate packet corruption on link vSwitchPart2 -> Node 4" \
Clean\ Link\ 4 "Remove the rules applied to link vSwitchPart2 -> Node 4" \
Break\ Link\ 5 "Simulate packet corruption on link vSwitchPart2 -> Node 5" \
Clean\ Link\ 5 "Remove the rules applied to link vSwitchPart2 -> Node 5" 2>"${INPUT}"

corr_menuitem=$(<"${INPUT}")

# make decsion 
case $corr_menuitem in
	Break\ Link\ 1) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s9 root netem corrupt 0.1%';;
	Clean\ Link\ 1) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s9 root';;
	Break\ Link\ 2) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s10 root netem corrupt 0.1%';;
	Clean\ Link\ 2) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s10 root';;
	Break\ Link\ 3) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s16 root netem corrupt 0.1%';;
	Clean\ Link\ 3) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s16 root';;
	Break\ Link\ 4) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s17 root netem corrupt 0.1%';;
	Clean\ Link\ 4) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s17 root';;
	Break\ Link\ 5) vagrant ssh switch -c 'sudo tc qdisc add dev enp0s18 root netem corrupt 0.1%';;
	Clean\ Link\ 5) vagrant ssh switch -c 'sudo tc qdisc del dev enp0s18 root';;
	Exit) echo "Bye"; break;;
esac

}

#########################################
#  	  	PROCESS STOP SIMULATION			#
#########################################

function stop_menu(){
	dialog --clear --backtitle "Raft Consensus Algorithm Testbed" \
--title "[ P R O C E S S  S T O P - S I M U L A T I O N ]" \
--menu "Simulate delay on links:" 20 80 8 \
Stop\ Process\ 1 "Simulate process crash on Node 1" \
Restart\ Process\ 1 "Restart process on Node 1" \
Stop\ Process\ 2 "Simulate process crash on Node 2" \
Restart\ Process\ 2 "Restart process on Node 2" \
Stop\ Process\ 3 "Simulate process crash on Node 3" \
Restart\ Process\ 3 "Restart process on Node 3" \
Stop\ Process\ 4 "Simulate process crash on Node 4" \
Restart\ Process\ 4 "Restart process on Node 4" \
Stop\ Process\ 5 "Simulate process crash on Node 5" \
Restart\ Process\ 5 "Restart process on Node 5" 2>"${INPUT}"

stop_menuitem=$(<"${INPUT}")

# make decsion 
case $stop_menuitem in
	Stop\ Process\ 1) echo "To be implemented";;
	Restart\ Process\ 1) echo "To be implemented";;
	Stop\ Process\ 2) echo "To be implemented";;
	Restart\ Process\ 2) echo "To be implemented";;
	Stop\ Process\ 3) echo "To be implemented";;
	Restart\ Process\ 3) echo "To be implemented";;
	Stop\ Process\ 4) echo "To be implemented";;
	Restart\ Process\ 4) echo "To be implemented";;
	Stop\ Process\ 5) echo "To be implemented";;
	Restart\ Process\ 5) echo "To be implemented";;
	Exit) echo "Bye"; break;;
esac

}

#########################################
# 			FAILURE SIMULATION			#
#########################################

function sim_menu(){
	dialog --clear --backtitle "Raft Consensus Algorithm Testbed" \
--title "[ S I M U L A T I O N ]" \
--menu "Choose which network behavior to simulate, the select the desired interface and insert the required parameters:" 15 80 8 \
Link\ Delay "Simulate link delay using SWITCH" \
Packet\ Loss "Simulate packet loss using SWITCH" \
Packet\ Duplication "Simulate packet duplication using SWITCH" \
Packet\ Corruption "Simulate packet corruption using SWITCH" \
Stop\ Process "Simulate process crash using NODE" 2>"${INPUT}"

sim_menuitem=$(<"${INPUT}")

# make decsion 
case $sim_menuitem in
	Link\ Delay) delay_menu;;
	Packet\ Loss) pktloss_menu;;
	Packet\ Duplication) dup_menu;;
	Packet\ Corruption) corr_menu;;
	Stop\ Process) stop_menu;;
	Exit) echo "Bye"; break;;
esac

}

#########################################
# 		  NETWORK PARTITIONING 			#
#########################################

function part_menu(){
	dialog --clear --backtitle "Raft Consensus Algorithm Testbed" \
--title "[ N E T - P A R T I T I O N I N G ]" \
--menu "Manage the virtual switches:" 15 80 8 \
Attach\ 1 "Attach the first network partition to the rest" \
Detach\ 1 "Detach the first network partition from the rest" \
Attach\ 2 "Attach the second network partition to the rest" \
Detach\ 2 "Detach the second network partition from the rest" 2>"${INPUT}"

sys_menuitem=$(<"${INPUT}")

# make decsion 
case $sys_menuitem in
	Attach\ 1) vagrant ssh switch -c 'sudo ovs-vsctl -- add-port vSwitchPart1 patch3 -- set interface patch3 type=patch options:peer=patch1';;
	Detach\ 1) vagrant ssh switch -c 'sudo ovs-vsctl del-port vSwitchPart1 patch3';;
	Attach\ 2) vagrant ssh switch -c 'sudo ovs-vsctl -- add-port vSwitchPart2 patch4 -- set interface patch4 type=patch options:peer=patch2';;
	Detach\ 2) vagrant ssh switch -c 'sudo ovs-vsctl del-port vSwitchPart2 patch4';;
	Exit) echo "Bye"; break;;
esac

}

#
# set infinite loop
#
while true
do

#########################################
# 			   MAIN MENU 				#
#########################################

dialog --clear --backtitle "Raft Consensus Algorithm Testbed" \
--title "[ C O N T R O L - C E N T E R ]" \
--menu "Raft Consensus Algorithm testbed manager." 15 80 8 \
System\ Utility "Manage the Vagrant testbed" \
Failure\ Simulation "Simulate network/ process failure" \
Network\ Partitioning "Simulate network partitioning" \
Exit "" 2>"${INPUT}"

menuitem=$(<"${INPUT}")


# make decsion 
case $menuitem in
	System\ Utility) sys_util;;
	Failure\ Simulation) sim_menu;;
	Network\ Partitioning) part_menu;;
	Exit) echo "Bye"; break;;
esac

done

# if temp files found, delete em
[ -f $OUTPUT ] && rm $OUTPUT
[ -f $INPUT ] && rm $INPUT