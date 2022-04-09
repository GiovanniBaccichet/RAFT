#!/bin/bash
# utilitymenu.sh - A sample shell script to display menus on screen
# Store menu options selected by the user
INPUT=/tmp/menu.sh.$$

# Storage file for displaying cal and date command output
OUTPUT=/tmp/output.sh.$$

# get text editor or fall back to vi_editor
vi_editor=${EDITOR-vi}

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
#
# Purpose - display current system date & time
#
function sys_util(){
	dialog --clear --backtitle "Raft Consensus Algorithm Testbed" \
--title "[ C O N T R O L - C E N T E R ]" \
--menu "Choose which network behavior to simulate, the select the desired interface and insert the required parameters:" 15 60 8 \
Startup "Start the testbed" \
Shut\ Down "Halt the testbed" \
Back "" 2>"${INPUT}"
}

sys_menuitem=$(<"${INPUT}")

# make decsion 
case $sys_menuitem in
	Startup) vagrant up;;
	Shut\ Down) vagrant halt;;
	Exit) echo "Bye"; break;;
esac
#
# Purpose - display a calendar
#
function sim_menu(){
	dialog --clear --backtitle "Raft Consensus Algorithm Testbed" \
--title "[ C O N T R O L - C E N T E R ]" \
--menu "Choose which network behavior to simulate, the select the desired interface and insert the required parameters:" 15 100 8 \
Link\ Delay "Simulate link delay using SWITCH" \
Packet\ Loss "Simulate packet loss using SWITCH" \
Packet\ Duplication "Simulate packet duplication using SWITCH" \
Packet\ Corruption "Simulate packet corruption using SWITCH" \
Stop\ Process "Simulate process crash using NODE" \
Back "" 2>"${INPUT}"
}

sim_menuitem=$(<"${INPUT}")

# make decsion 
case $sim_menuitem in
	System\ Utility) sys_util;;
	Simulation) sim_menu;;
	Exit) echo "Bye"; break;;
esac
#
# set infinite loop
#
while true
do

### display main menu ###
dialog --clear --backtitle "Raft Consensus Algorithm Testbed" \
--title "[ C O N T R O L - C E N T E R ]" \
--menu "Choose which network behavior to simulate, the select the desired interface and insert the required parameters:" 15 60 8 \
System\ Utility "Manage the Vagrant testbed" \
Simulation "Manage the network simulation" \
Exit "" 2>"${INPUT}"

menuitem=$(<"${INPUT}")


# make decsion 
case $menuitem in
	System\ Utility) sys_util;;
	Simulation) sim_menu;;
	Exit) echo "Bye"; break;;
esac

done

# if temp files found, delete em
[ -f $OUTPUT ] && rm $OUTPUT
[ -f $INPUT ] && rm $INPUT