### Firecracker VM

#### Prerequisites

- Follow the [Quick Start Guide](https://github.com/firecracker-microvm/firecracker/blob/master/docs/getting-started.md) to install firecracker microVM.  
- Alternatively, if available, install the package `aur/firecracker-git`.  
- Binaries `kernel_image_path` and `rootfs` must be placed in firecracker/bin/.  

Make sure firecracker can create its API socket:
```
rm -f /tmp/firecracker.socket
```

#### Run firecracker

```
firecracker --api-sock /tmp/firecracker.socket --config-file firecracker/vmConfig.json
```

