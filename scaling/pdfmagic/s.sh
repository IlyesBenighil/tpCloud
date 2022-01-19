gcloud compute images create ubuntu-with-vmx \
--source-disk ubuntu \
--licenses "https://www.googleapis.com/compute/v1/projects/vm-options/global/\
licenses/enable-vmx"
