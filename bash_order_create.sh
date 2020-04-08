. ./bash_deploy.sh

echo "[[[ Order.sh/order_create payable=ff(hex) penalty_percentage_seller=0(num) penalty_percentage_buyer=0(num) ]]]"
java -jar ./avm/avm.jar call $contract -m order_create -a -BI ff -B 0 -B 0

#echo "[[[ Order.sh/order_create value=128(num) payable=0100(hex) penalty_percentage_seller=50(num) penalty_percentage_buyer=50(num) ]]]"
#java -jar ./avm/avm.jar call $contract -m order_create --value 128 -a -BI 0100 -B 50 -B 50
#
## get order by id
#echo "[[[ Order.sh/order_by_id id=0 ]]]"
#java -jar ./avm/avm.jar call $contract -m order_by_id -a -J 0
#
## get order by id
#echo "[[[ Order.sh/order_by_id id=1 ]]]"
#java -jar ./avm/avm.jar call $contract -m order_by_id -a -J 1