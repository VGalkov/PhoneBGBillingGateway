package phoneBGBillingGateway;

public class PhoneBGBillingGateway {
/*
задолбали эти конфликты меду mysql и mariaDB
тут будет модуль, который заменит freeradius и проверку скриптом phone
а так же скрипт, который переодически тасует логи и папки, куда кладёт мера в формат бгбиллинга.

запустить тут процесс прекладки логов и прослушивание порта 1912 по протоколу радиуса в нудном формате и не болеее.  

фактически это модуль-костыль для телефонии между мераРТУ и биллингом. 

*/
private static final String Throw = null;
/*


# ------------------------------- тестовый костыль. заменить на номер направления.
#$ARGV[2] = '02';
#$ARGV[0] = '2300033';
#$ARGV[2] = '9023725958';
#$ARGV[2] = '01'; # = $ARGV[2] - номер, куда звонит абонент.
#$ARGV[2] = '9272006026';
# ---------------- тумблеры проверок:
#
#-----------------------------------
*/
	
	public static void main(String[] args) {
		try {
			if (args.length!=4) { throw new RuntimeException("не полный комплект данных"); }
			else {  
				Calculator calc = new Calculator(args);
				if (calc.prepareAnswer()) {
					//System.out.print(calc.getMessage()); это в лог
					System.out.print(calc.getAnswer());
					System.exit(calc.getExitCode());
				}
				else throw new RuntimeException(calc.getAnswer()+"::"+calc.getMessage()+"("+calc.getExitCode()+")");
			}
		}
		catch(RuntimeException e) {
			// дописать мессагу в лог
		 System.out.print("Reply-Message = \"[*]Нет данных!\"");
		 System.exit(-1);
		}
		finally { System.exit(-1); }
	}
	
	
}
