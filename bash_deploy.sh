#!/bin/bash

jobs="$(./bash_compile.sh dev.makkii.wallet.v1.Batch ./src/dev/makkii/Constant.java ./src/dev/makkii/Util.java ./src/dev/makkii/wallet/v1/Batch.java  && java -jar ./avm/avm.jar deploy ./build/dapp.jar)"
#jobs="$(./bash_compile.sh dev.makkii.wallet.ETF ./src/dev/makkii/*.java ./src/dev/makkii/wallet/ETF.java  && java -jar ./avm/avm.jar deploy ./build/dapp.jar)"
line_sender="";line_contract="";line_energy="";line_result=""
while IFS= read
do
    # echo $REPLY
    if [[ $REPLY =~ "Sender" ]]; then line_sender=$REPLY; fi
    if [[ $REPLY =~ "Dapp Address" ]]; then line_contract=$REPLY; fi
    if [[ $REPLY =~ "Energy cost" ]]; then line_energy=$REPLY; fi
    if [[ $REPLY =~ "Result status" ]]; then line_result=$REPLY; fi
done <<< "$jobs"
sender=${line_sender/Sender       : /}
contract=${line_contract/Dapp Address : /}
energy=${line_energy/Energy cost  : /}
result=${line_result/Result status: /}
printf "\n[contract] $contract\n  [sender] $sender\n  [energy] $energy\n  [result] $result\n"
export sender=$sender;
export CONTRACT=$contract;
export ENERGY=$energy;
export RESULT=$result;

cp build/dapp.jar ../tool-deploy && cd ../tool-deploy && node index.js && cd ../contract-wallet