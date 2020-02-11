package phoneBGBillingGateway;

public class PhoneBGBillingGateway {
/*
��������� ��� ��������� ���� mysql � mariaDB
��� ����� ������, ������� ������� freeradius � �������� �������� phone
� ��� �� ������, ������� ������������ ������ ���� � �����, ���� ����� ���� � ������ ����������.

��������� ��� ������� ��������� ����� � ������������� ����� 1912 �� ��������� ������� � ������ ������� � �� ������.  

���������� ��� ������-������� ��� ��������� ����� ������� � ���������. 

*/
private static final String Throw = null;
/*


# ------------------------------- �������� �������. �������� �� ����� �����������.
#$ARGV[2] = '02';
#$ARGV[0] = '2300033';
#$ARGV[2] = '9023725958';
#$ARGV[2] = '01'; # = $ARGV[2] - �����, ���� ������ �������.
#$ARGV[2] = '9272006026';
# ---------------- �������� ��������:
#
#-----------------------------------
*/
	
	public static void main(String[] args) {
		try {
			if (args.length!=4) { throw new RuntimeException("�� ������ �������� ������"); }
			else {  
				Calculator calc = new Calculator(args);
				if (calc.prepareAnswer()) {
					//System.out.print(calc.getMessage()); ��� � ���
					System.out.print(calc.getAnswer());
					System.exit(calc.getExitCode());
				}
				else throw new RuntimeException(calc.getAnswer()+"::"+calc.getMessage()+"("+calc.getExitCode()+")");
			}
		}
		catch(RuntimeException e) {
			// �������� ������� � ���
		 System.out.print("Reply-Message = \"[*]��� ������!\"");
		 System.exit(-1);
		}
		finally { System.exit(-1); }
	}
	
	
}
