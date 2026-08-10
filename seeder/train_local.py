from pathlib import Path

from gtfs_model.train_model import train_and_save_model


if __name__ == '__main__':
    workspace = Path(__file__).resolve().parent
    train_and_save_model(
        stop_times_path=workspace / 'gtfs' / 'stop_times.txt',
        trips_path=workspace / 'gtfs' / 'trips.txt',
        guide_csv_path=workspace / 'cleaned_user_guides.csv',
        output_dir=workspace,
    )
