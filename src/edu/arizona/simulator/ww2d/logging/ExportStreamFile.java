package edu.arizona.simulator.ww2d.logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExportStreamFile {
	
	/**
	 * Change this function when you want to change which
	 * file is output and where it is output to.
	 * @param args
	 */
	public static void main(String[] args) { 
		String path = "states/";
		
		int min = 1;
		int max = 20;
		String[] activity = { "ball", "chase", "column", "eat", "fight", "wander" };

		for (String base : activity) { 
			for (int cnt = min; cnt <= max; ++cnt) {

				String statePath = path + base + "-" + cnt + "/";
				String inputDB = statePath + "state-agent1.db";
				String outputFile = "logs/" + base + "-" + cnt + ".csv";

				StateDatabase db = new StateDatabase(inputDB, false);
				Map<Integer,String> entityMap = new HashMap<Integer,String>();
				Map<Integer,String> fluentMap = new HashMap<Integer,String>();

				fillMap(db, "select rowid, name from lookup_fluent_table", fluentMap);
				fillMap(db, "select rowid, name from entity_map_table", entityMap);

				System.out.println("Fluents: " + fluentMap.size() + " Entities: " + entityMap.size());

				int maxTime = 0;  // this will contain the size of the stream to be created.
				Map<String,List<Row>> rowMap = new HashMap<String,List<Row>>();
				for (String fluent : fluentMap.values()) { 
					String tableName = "fluent_" + fluent;

					List<Row> rows = new ArrayList<Row>();
					try { 
						Statement s = db.getStatement();
						ResultSet rs = s.executeQuery("select * from " + tableName);
						while (rs.next()) { 
							Row r = new Row();
							r.fluent = fluent;
							r.entities = entityMap.get(rs.getInt("entities_id")).replaceAll("[,]", "");
							r.value = rs.getString("value");
							r.start = rs.getInt("start_time");
							r.end = rs.getInt("end_time");
							rows.add(r);

							maxTime = Math.max(r.end, maxTime);
						}
						rs.close();
					} catch (Exception e) { 
						e.printStackTrace();
					}
					rowMap.put(fluent, rows);
				}


				// now we need to convert each fluent/entities pair into a stream
				Map<String,String[]> streamMap = new TreeMap<String,String[]>();
				for (String fluent : fluentMap.values()) { 
					for (Row r : rowMap.get(fluent)) { 
						// first check to see if we already created a stream for this entity.  If not add it.
						String key = "\"" + fluent + "(" + r.entities + ")\"";
						String[] stream = streamMap.get(key);
						if (stream == null) { 
							stream = new String[maxTime];
							streamMap.put(key, stream);
						}

						for (int i = r.start; i < r.end; ++i) { 
							// if you want numerical values instead of "true" and "false" and "unknown"
							// then uncomment out this line.  Otherwise, leave it be.
							//					updateNumeric(stream, r.value, i);
							stream[i] = r.value;
						}
					}
				}

				// now we can write the mofo out.
				try { 
					BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
					StringBuffer header = new StringBuffer();
					for (String name : streamMap.keySet()) { 
						header.append(name + ",");
					}	
					header.deleteCharAt(header.length()-1);
					out.write(header + "\n");

					// time starts at 1 so we should too.
					for (int i = 1; i < maxTime; ++i) { 
						StringBuffer row = new StringBuffer();
						for (Map.Entry<String,String[]> entry : streamMap.entrySet()) { 
							String value = entry.getValue()[i];
							row.append(value + ",");
						}
						row.deleteCharAt(row.length()-1);
						out.write(row + "\n");
					}
					out.close();
				} catch (Exception e) { 
					e.printStackTrace();
				}
			}
		}		
	}
	
	/**
	 * Update the stream at position i with the value 
	 * @param stream
	 * @param r
	 * @param i
	 */
	private static void updateNumeric(String[] stream, String value, int i) { 
		if ("false".equals(value)) {
			stream[i] = "0";
		} else if ("true".equals(value)) { 
			stream[i] = "1";
		} else if ("unknown".equals(value)){
			stream[i] = "-1";
		} else { 
			stream[i] = value;
		}
	}
	
	/**
	 * Populate a map of integers mapped to the name of the thing.
	 * @param db
	 * @param sql
	 * @param map
	 */
	private static void fillMap(StateDatabase db, String sql, Map<Integer,String> map) { 
		try { 
			Statement s = db.getStatement();
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt("rowid");
				String name = rs.getString("name");
				map.put(id, name);
			}
			rs.close();
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}
}

class Row { 
	public String fluent;
	public String entities;
	
	public String value;
	public int start;
	public int end;
}