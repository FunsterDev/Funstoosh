module Concerns::ErrorHandling
  extend ActiveSupport::Concern

  included do
    rescue_from ActiveRecord::RecordNotFound, with: :handle_record_not_found
    rescue_from ActiveRecord::RecordInvalid, with: :handle_record_invalid
  end

  def handle_record_not_found
    head :not_found
  end

  def handle_record_invalid
    head :unprocessable_entity
  end
end
