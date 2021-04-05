Vagrant.configure("2") do |config|
   config.vm.define :ubuntu do |ubuntu|
      ubuntu.vm.provider "virtualbox" do |v|
         v.customize ["modifyvm", :id, "--name", "ubuntu", "--memory", "2048"]
      end
      ubuntu.vm.box = "ubuntu/trusty64"
      ubuntu.vm.hostname = "ubuntu"
      ubuntu.vm.network :public_network, ip: "192.168.31.225"
   end
end