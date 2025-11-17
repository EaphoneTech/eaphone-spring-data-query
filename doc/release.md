# How to release

1. develop using `xxx-SNAPSHOT` version
2. run all tests
3. when preparing to release, run following steps:
  1. run `release:prepare -DdryRun`
  2. run `release:prepare`
  
    in this step, a git tag will be created and pushed to origin. 
  
  3. run `release:perform -DdryRun`
  4. run `release:perform`
  
    in this step, the version will be bumpted to the next snapshot.
    
5. merge to `master` branch
6. push `master` to `cnb` origin
7. `cnb.cool` will trigger CI in `flow.aliyun.com` and deploy
