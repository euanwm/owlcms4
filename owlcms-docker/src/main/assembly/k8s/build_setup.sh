#/bin/bash -wq
(echo "# DockerDesktop setup"; kubectl kustomize overlays/dd ) > dd_setup.yaml
(echo "# k3s setup "; kubectl kustomize overlays/k3s ) > k3s_setup.yaml
(echo "# k3d setup "; kubectl kustomize overlays/k3d ) > k3d_setup.yaml
(echo "# k3d nocert setup "; kubectl kustomize overlays/k3d-nocert ) > k3d_nocert.yaml
echo done.