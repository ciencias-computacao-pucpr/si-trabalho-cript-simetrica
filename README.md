#### Desenvolva um programa que implemente o centro de distribuição de chaves (KDC). O programa é composto de duas entidades (Alice e Bob) que desejam conversar utilizando criptografia simétrica. Os seguintes requisitos devem ser atendidos:

1. Bob e o KDC devem compartilhar uma chave mestre;
1. Alice e o KDC devem compartilhar uma chave mestre;
1. Bob e Alice devem conversar através de uma chave de sessão;
1. A chave de sessão deve ser obtida através de uma comunicação criptografada com o KDC, utilizando a chave mestre;
1. Quando ambas entidades possuírem a chave de sessão, Bob gera um nonce e encaminha para Alice, cifrando na ;
1. Alice responde Bob executando uma função sobre o nonce recebido, cifrando na chave de sessao;
1. Bob compara o valor recebido com o valor de nonce enviado realizando a função;

*Observação: O programa pode ser desenvolvido em qualquer linguagem de programação. A troca de mensagens entre as entidades pode ser realizada através métodos ou funções.*