
## 准备证书
ssh-keygen

## 安装软件 git/maven/node/docker/docker-compose
sudo apt install curl


sudo apt install git
git -version

java -version

sudo apt install maven
mvn -version




sudo apt install nodejs
node -version

sudo apt install yarn

curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash

# 启用脚本
source ~/.bashrc
source ~/.profile


#sudo apt install docker.io

## 当前用户直接用docker
# 添加docker group：
sudo groupadd docker
# 将当前用户添加到docker组：
sudo usermod -aG docker ${USER}
#sudo gpasswd -a ${USER} docker
# 添加执行权限
sudo chmod a+rw /var/run/docker.sock
# 重启docker服务：
sudo service docker restart
# 查看用户组及成员：
cat /etc/group | grep docker

