def jobsMapping = [
  tags: [jobName:"App GWAPortal", jobTags: "reload", extraVars: "app_generic_image_tag: latest"]
]

buildDockerImage([
    imageName: "gwaportal-backend",
    pushRegistryNamespace: "nordborglab/gwaportal",
    pushBranches: ['develop','master', 'refactor'],
    tower: jobsMapping
])