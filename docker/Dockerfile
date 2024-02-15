FROM rockstorm/git-server:2.43

RUN mkdir -p /srv/git/repos/rejecting-repo \
  && mkdir -p /home/git/.ssh \
  && echo -e "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCu0nkKWfkHn7bqz4VwFNORWZYZp1jKuiom/E3I5XzGMZTmo2M9TWIRGPsx1h+5GYnFiGo8DYaJv1T//nnE2lAWJ7e9Cj9dJQ5wx3EwJc9twHEzBF8hstpzCZS2UVlsWlkKkVwH8py54wh/xoG1mcAH/am5QBxcFarHqmMaN9YU6tXv2ipbpd5BsXUDvh0WOS/j/iylD2ACUYe+iBzC/FrZDeJR6Kkbomb+1Pxr7ffail5WzihpHcF6lR1hG98f8pDtJUo00n5mmZPp3ZxFmssIHbv9ZdU6x0vgEs9qBO1p0tILhVLhNHq3oLumWRtEzdH7AP1VjNCQ3aMy4MpJo+xUHD28btWS5N4wVpAQCZqVu5ucz/5jnsdluQVEd4grUu4nKgFiHPA0/o938fDO7tO2HOp3QhdFK+zlP6Q0H4XOZTk3kYn+9yymT294lqM+NeFApSdGSCROJI5HZQaQJX2tkjAy5eJYQcBzko6+KVL+mWZ8/D54NJX0O87FN0205NM= user@host" >> /home/git/.ssh/authorized_keys \
  && chown -R git:git /home/git/.ssh \
  && chmod 700 /home/git/.ssh \
  && chmod 600 /home/git/.ssh/authorized_keys \
  && git init --bare /srv/git/repos/rejecting-repo \
  && echo -e "#!/bin/sh\necho 'I reject this push!' >&2\nexit 1" > /srv/git/repos/rejecting-repo/hooks/pre-receive \
  && chmod +x /srv/git/repos/rejecting-repo/hooks/pre-receive

CMD ["/usr/sbin/sshd", "-D"]