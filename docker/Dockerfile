FROM jkarlos/git-server-docker

RUN passwd -d git \
  && sed -i 's/^PasswordAuthentication.*/PasswordAuthentication yes/g' /etc/ssh/sshd_config \
  && echo "PermitEmptyPasswords yes" >> /etc/ssh/sshd_config \
  && ls /etc/init.d \
  && mkdir -p repos \
  && git init --bare repos/rejecting-repo \
  && echo -e "#!/bin/sh\necho 'I reject this push!' >&2\nexit 1" > repos/rejecting-repo/hooks/pre-receive \
  && chmod +x repos/rejecting-repo/hooks/pre-receive \
  && git init --bare repos/existing-tag-repo \
  && sh /etc/init.d/sshd restart

CMD ["sh", "start.sh"]
