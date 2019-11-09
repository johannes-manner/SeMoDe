FROM archlinux/base

WORKDIR /usr/src/linpack
COPY linpack/local/ .

RUN pacman --noconfirm -Sy gawk
RUN pacman --noconfirm -Sy numactl

CMD ["./runme64"]
