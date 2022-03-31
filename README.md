<div id="top"></div>
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->


<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/GiovanniBaccichet/RAFT">
    <img src="media/raft.png" alt="Logo" width="250">
  </a>

<h3 align="center">The Raft Consensus Algorithm</h3>

  <p align="center">
    A Java implementation using sockets 🗳
    <br />
    <a href="https://raft.github.io/"><strong>Raft Whitepaper »</strong></a>
    <br />
    <br />
    <a href="https://github.com/GiovanniBaccichet/RAFT">Docs 📓</a>
    ·
    <a href="https://github.com/GiovanniBaccichet/RAFT/issues">Report Bug 🪳</a>
    ·
    <a href="https://github.com/GiovanniBaccichet/RAFT/issues">Request Feature ✨</a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project 🧬

Raft is a **consensus algorithm** that is designed to be easy to understand. It's **equivalent to Paxos in fault-tolerance and performance**. The difference is that it's decomposed into relatively independent sub-problems, and it cleanly addresses all major pieces needed for practical systems. We hope Raft will make consensus available to a wider audience, and that this wider audience will be able to develop a variety of higher quality consensus-based systems than are available today.

<p align="center">
  <img src"media/raft_animation.gif">
</p>

Consensus is a fundamental problem in fault-tolerant distributed systems. Consensus involves multiple servers agreeing on values. Once they reach a decision on a value, that decision is final. Typical consensus algorithms make progress when any majority of their servers is available; for example, a cluster of 5 servers can continue to operate even if 2 servers fail. If more servers fail, they stop making progress (but will never return an incorrect result).

Consensus typically arises in the context of replicated state machines, a general approach to building fault-tolerant systems. Each server has a state machine and a log. The state machine is the component that we want to make fault-tolerant, such as a hash table. It will appear to clients that they are interacting with a single, reliable state machine, even if a minority of the servers in the cluster fail. Each state machine takes as input commands from its log. In our hash table example, the log would include commands like set $x$ to 3. A consensus algorithm is used to agree on the commands in the servers' logs. The consensus algorithm must ensure that if any state machine applies set $x$ to 3 as the $n^{th}$ command, no other state machine will ever apply a different $n^{th}$ command. As a result, each state machine processes the same series of commands and thus produces the same series of results and arrives at the same series of states.

<p align="right">(<a href="#top">back to top</a>)</p>



### Built With 🏗

* [Java](https://www.java.com/it/)

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started

This is an example of how you may give instructions on setting up your project locally.
To get a local copy up and running follow these simple example steps.

### Prerequisites

This is an example of how to list things you need to use the software and how to install them.
* npm
  ```sh
  npm install npm@latest -g
  ```

### Installation

1. Get a free API Key at [https://example.com](https://example.com)
2. Clone the repo
   ```sh
   git clone https://github.com/GiovanniBaccichet/RAFT.git
   ```
3. Install NPM packages
   ```sh
   npm install
   ```
4. Enter your API in `config.js`
   ```js
   const API_KEY = 'ENTER YOUR API';
   ```

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- USAGE EXAMPLES -->
## Usage

Use this space to show useful examples of how a project can be used. Additional screenshots, code examples and demos work well in this space. You may also link to more resources.

_For more examples, please refer to the [Documentation](https://raft.github.io/)_

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- ROADMAP -->
## Roadmap 🛣

- [ ] Feature 1
- [ ] Feature 2
- [ ] Feature 3
    - [ ] Nested Feature

See the [open issues](https://github.com/GiovanniBaccichet/RAFT/issues) for a full list of proposed features (and known issues).

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- TESTING -->
## Testing 🧪

Since the algorithm is <mark>robust against any non-byzantine failure, links can have omissions and delays, processes can stop at any time</mark>. For this reason we decided to deploy 5 hosts and simulate the different behaviors our implementation could have in a controlled environment. The network topology is shown in the diagram below.


[![Network topology - testing the implementation][net-topology]](https://github.com/GiovanniBaccichet/RAFT)

In order to work with a controlled environment, to properly asses the correctness of the implementation, we decided to virtualize the 5 hosts, plus a router and a switch to network them together. The challenge was to test the above mentioned failures:

- Links can have omissions → <mark>explain more in detail</mark>
- Links can have delays → <mark>explain more in detail</mark>
- Processes can stop at any time → <mark>explain more in detail</mark>

The software of choice for creating a suitable lab for testing purposes was **Vagrant** (and **VirtualBox**): both softwares are open source and offer the required capabilities for handling link failure, as well as process failure. This will be explained more in depth in the following.

```
.
└── RAFT/
    ├── vagrant/
    │   ├── client.sh
    │   ├── node.sh
    │   ├── router.sh
    │   └── switch.sh
    └── Vagrantfile
```

As shown in the box above, the VMs are creating following what's inside the `Vagrantfile` and configured based on the respective bash scripts contained in `vagrant/`.
Network failure is simulated using [netem](https://wiki.linuxfoundation.org/networking/netem#delay_distribution), a tool that comes built-in Linux. This allows to simulate:
- **Delay Listribution**: `tc qdisc change dev eth0 root netem delay 100ms 20ms distribution normal`

- **(Random) Packet Loss**: `tc qdisc change dev eth0 root netem loss 0.1%`

- **Packet Duplication**: `tc qdisc change dev eth0 root netem duplicate 1%`

- **Packet Corruption**: `tc qdisc change dev eth0 root netem corrupt 0.1%`

Another useful feature of netem is the capability to control the bandwidth using the `rate` feature. Another options could have been playing with `iptables`.

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- CONTRIBUTING -->
## Contributing ❤️

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- LICENSE -->
## License 📑

Distributed under the GPLv3 License. See `LICENSE` for more information.

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- CONTACT -->
## Contact 📬

Giovanni Baccichet - [@Giovanni_Bacci](https://twitter.com/Giovanni_Bacci) - `giovanni.baccichet[at].polimi.it`

Chiara Magri - `chiara.magri[at]mail.polimi.it`

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments 🥸

* [The Raft Consensus Algorithm](https://raft.github.io/)
* [netem](https://wiki.linuxfoundation.org/networking/netem#delay_distribution)
* []()

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->

[raft-animation]: media/raft_animation.gif

[net-topology]: media/net_topology.png
