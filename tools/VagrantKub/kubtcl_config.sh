# Get password from master config file
PASSWORD=$(vagrant ssh kmaster -c "sudo grep password /etc/rancher/k3s/k3s.yaml" | awk -F':' '{print $2}' | sed 's/ //g')

#Create kubectl config
cat << EOF > ./kubectlvagrantconfig.yml
apiVersion: v1
clusters:
- cluster:
    server: https://10.0.0.30:6443
    insecure-skip-tls-verify: true
  name: default
contexts:
- context:
    cluster: default
    user: default
  name: default
current-context: default
kind: Config
preferences: {}
users:
- name: default
  user:
    password: ${PASSWORD}
    username: admin
EOF
