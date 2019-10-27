### Firecracker VM

#### Prerequisites

- Follow the [Quick Start Guide](https://github.com/firecracker-microvm/firecracker/blob/master/docs/getting-started.md) to install firecracker microVM.  
- Alternatively, if available, install the package `aur/firecracker-git`.  
- Binaries `kernel_image_path` and `rootfs` must be placed in firecracker/bin/.  


#### Defining the Network Bridge

Useful links:
- [firecracker network docs](https://github.com/firecracker-microvm/firecracker/blob/master/docs/network-setup.md)
- [LWN Documentation](https://lwn.net/Articles/775736/)
- [Use Docker bridge for NAT](https://github.com/firecracker-microvm/firecracker/issues/711#issuecomment-450928398)

Firecracker uses a `TAP interface` on the host.
Create an TAP interface on the host:
```sh
ip tuntap add dev tap0 mode tap

# Option 1
ip addr add 172.17.0.1/16 dev tap0
# Option 2 - (worked for me)
brctl addif docker0 tap0

ip link set tap0 up
```

#### Run firecracker

```sh
# Make sure firecracker can create its API socket:
rm -f /tmp/firecracker.socket
# Replace network address in vmConfig
firecracker --api-sock /tmp/firecracker.socket --config-file firecracker/vmConfig.json
```

Log in as `root` (pw=`root`).

```sh
ip link set eth0 up
ip addr add dev eth0 172.17.0.3/16

# Try network with ping
```


Stop firecracker by sending a kill/ reboot command.
