mkdir code
aws lambda list-functions --profile siryus | \
grep FunctionName | \
cut -d '"' -f4 | \
while read -r name; do
	aws lambda delete-function --profile siryus --function-name $name 
done