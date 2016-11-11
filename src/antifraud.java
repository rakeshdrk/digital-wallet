import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class antifraud 
{
	private static final String TRUSTED = "trusted";
	private static final String UNVERIFIED = "unverified";
	public static void main(String[] args) throws IOException
	{
		if(args.length < 5)
		{
			return;
		}

		String batch_text = args[0];
		String stream_text = args[1];
		String feature1 = args[2];
		String feature2 = args[3];
		String feature3 = args[4];
		HashMap<String,HashSet<String>> userMapping = new HashMap<String,HashSet<String>>();
		createuserMapping(batch_text, userMapping);
		checkTransactions(stream_text,feature1, feature2, feature3, userMapping);
	}
	
	private static void createuserMapping(String batch_text, HashMap<String, HashSet<String>> userMapping) throws IOException {
		BufferedReader input = null ;
		try {
			input = new BufferedReader(new FileReader(batch_text));
			input.readLine();
			String line = input.readLine();
			while(line!=null)
			{
				String arr[] = line.split(",");
				if(arr.length < 5)
				{
					line = input.readLine();
					continue;
				}
				String user1 = arr[1].trim();
				String user2 = arr[2].trim();
				if(userMapping.get(user1) == null)
				{
					userMapping.put(user1,new HashSet<String>());
				}
				if(userMapping.get(user2) == null)
				{
					userMapping.put(user2,new HashSet<String>());
				}
				HashSet<String> userlist = userMapping.get(user1);
				userlist.add(user2);
				userlist = userMapping.get(user2);
				userlist.add(user1);
				line = input.readLine();
			}
		}
		catch(Exception e) {
			input.close();
			e.printStackTrace();
		}
	}
	
	private static void checkTransactions(String stream_text, String feature1,
			String feature2, String feature3,
			HashMap<String, HashSet<String>> userMapping) throws IOException {
		BufferedReader input = null;
		BufferedWriter output1 = null;
		BufferedWriter output2 = null;
		BufferedWriter output3 = null;
		try {
			input = new BufferedReader(new FileReader(stream_text));
			output1 = (new BufferedWriter(new FileWriter(feature1))); 
			output2 = new BufferedWriter(new FileWriter(feature2));
			output3 = new BufferedWriter(new FileWriter(feature3));
			input.readLine();
			String line = input.readLine();
			while(line != null)
			{
				String arr[] = line.split(",");
				if(arr.length < 5)
				{
					line = input.readLine();
					continue;
				}	
				String user1 = arr[1].trim();
				String user2 = arr[2].trim();
				if((userMapping.get(user1) == null) || (userMapping.get(user2) == null) )
				{
					output1.append(UNVERIFIED);
					output2.append(UNVERIFIED);
					output3.append(UNVERIFIED);
				}
				else if(userMapping.get(user1).contains(user2))
				{
					output1.append(TRUSTED);
					output2.append(TRUSTED);
					output3.append(TRUSTED);
				}else {
					output1.append(UNVERIFIED);
					HashSet<String> userList = userMapping.get(user1);
					boolean isFound = false;
					for(String user : userList)
					{
						if(userMapping.get(user).contains(user2))
						{
							isFound = true;
							output2.append(TRUSTED);
							output3.append(TRUSTED);
						}
					}
					if(!isFound)
					{
						output2.append(UNVERIFIED);
						HashSet<String> isChecked = new HashSet<String>();
						for(String fromuser : userList)
						{
							if(isChecked.contains(fromuser))
								continue;
							isChecked.add(fromuser);
							int connection = checkConnection(fromuser,user2,isChecked, userMapping, 2);
							if(connection <= 4 && connection >= 0)
							{
								isFound=true;
								output3.append(TRUSTED);
								break;
							}
						}
					}
					if(!isFound)
					{
						output3.append(UNVERIFIED);
					}
				}
				line = input.readLine();
				output1.newLine();
				output2.newLine();
				output3.newLine();
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			input.close(); 
			output1.close(); 
			output2.close();
			output3.close(); 
		}
		
	}

	private static int checkConnection(String fromUser, String checkUser,
			HashSet<String> isChecked,
			HashMap<String, HashSet<String>> userMapping, int edge) {
		if(edge > 4)
			return -1;
		HashSet<String> userList = userMapping.get(fromUser);
		if(userList.contains(checkUser))
		{
			return edge;
		}
		for(String frmUser : userList)
		{
			if(isChecked.contains(frmUser))
				continue;
			isChecked.add(frmUser);
			int connection = checkConnection(frmUser,checkUser,isChecked, userMapping, edge+1);
			if(connection == 3 || connection == 4)
			{
				return connection;
			}
		}
		return -1;
	}

}
